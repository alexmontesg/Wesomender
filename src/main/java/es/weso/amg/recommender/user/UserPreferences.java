package es.weso.amg.recommender.user;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import es.weso.amg.recommender.model.Article;
import es.weso.amg.recommender.model.Rating;
import es.weso.amg.recommender.persistence.ArticleDAO;
import es.weso.amg.recommender.persistence.RatingDAO;
import es.weso.amg.recommender.util.ValueComparatorDesc;

public class UserPreferences {

	private String user_id;
	private MongoDBDataModel model;
	private Recommender recommender;
	private ArticleDAO adao;
	private RatingDAO rdao;
	private double lat, lon;

	private static final Comparator<Rating> TIME_ORDER = new Comparator<Rating>() {
		public int compare(Rating r1, Rating r2) {
			if (r1.getCreated_at() == r2.getCreated_at()) {
				return r2.compareTo(r1);
			}
			return (r1.getCreated_at() < r2.getCreated_at() ? 1 : -1);
		}
	};

	public UserPreferences(String user_id, MongoDBDataModel model,
			Recommender recommender, double lat, double lon) {
		this.user_id = user_id;
		this.model = model;
		this.recommender = recommender;
		rdao = new RatingDAO();
		adao = new ArticleDAO();
		this.lat = lat;
		this.lon = lon;
	}

	public Collection<Rating> getPreferences(double collaborativeFiltering,
			int lastN) throws TasteException {
		Collection<Rating> ratings = getMostRecent(lastN);
		return getPreferences(collaborativeFiltering, ratings);
	}

	public Collection<Rating> getPreferences(double collaborativeFiltering,
			long timestamp, long threshold) throws TasteException {
		Collection<Rating> ratings = getMostRecent(timestamp, threshold);
		return getPreferences(collaborativeFiltering, ratings);
	}

	private Collection<Rating> getPreferences(double collaborativeFiltering,
			Collection<Rating> ratings) throws TasteException {
		Map<String, Double> genres = getMostValuedGenres(ratings,
				model.getMaxPreference() / 2);
		double contentBased = 1 - collaborativeFiltering;
		Collection<Long> unratedArticles = getUnratedArticles();

		Collection<Rating> estimatedRatings = new ArrayDeque<Rating>(
				unratedArticles.size());
		long long_user_id = Long.parseLong(model.fromIdToLong(user_id, true));
		Collection<String> haveCollaborativeScore = getHybridPreferences(
				collaborativeFiltering, genres, contentBased, unratedArticles,
				estimatedRatings, long_user_id);

		getContentBasedPreferences(genres, contentBased, estimatedRatings,
				long_user_id, haveCollaborativeScore);

		return estimatedRatings;
	}

	protected Collection<String> getHybridPreferences(
			double collaborativeFiltering, Map<String, Double> genres,
			double contentBased, Collection<Long> unratedArticles,
			Collection<Rating> estimatedRatings, long long_user_id)
			throws TasteException {
		List<RecommendedItem> recommendedItems = recommender.recommend(
				long_user_id, unratedArticles.size());
		Collection<String> haveCollaborativeScore = new ArrayDeque<String>(
				recommendedItems.size());
		for (RecommendedItem item : recommendedItems) {
			float collaborativeScore = item.getValue();
			String item_id = model.fromLongToId(item.getItemID());
			haveCollaborativeScore.add(item_id);
			double score = collaborativeFiltering * collaborativeScore
					+ contentBased
					* getContentBasedRating(genres, adao.getArticle(item_id));
			estimatedRatings.add(new Rating(user_id, item_id, score, System
					.currentTimeMillis()));
		}
		return haveCollaborativeScore;
	}

	protected void getContentBasedPreferences(Map<String, Double> genres,
			double contentBased, Collection<Rating> estimatedRatings,
			long long_user_id, Collection<String> haveCollaborativeScore)
			throws TasteException {
		LongPrimitiveIterator iter = model.getItemIDsFromUser(long_user_id)
				.iterator();
		while (iter.hasNext()) {
			haveCollaborativeScore.add(model.fromLongToId(iter.next()));
		}

		Collection<Article> dontHaveCollaborativeScore = adao
				.getArticlesNotIn(haveCollaborativeScore);

		for (Article article : dontHaveCollaborativeScore) {
			double score = contentBased
					* getContentBasedRating(genres, article);
			estimatedRatings.add(new Rating(user_id, article.getItem_id(),
					score, System.currentTimeMillis()));
		}
	}

	private double getContentBasedRating(Map<String, Double> valuedEntities,
			Article article) {
		double score = 0.0;
		for (Map.Entry<String, Double> entry : valuedEntities.entrySet()) {
			if (article.hasEntity(entry.getKey())) {
				score += entry.getValue() / 100;
			}
		}
		score *= 0.15;
		score += 0.5 * (1.0 - (System.currentTimeMillis() - article.getTimestamp())
				/ (double) System.currentTimeMillis());
		double maxDistance = 20037.58;
		double distance = calculateDistance(lat, lon, article.getLat(),
				article.getLon());
		score += 0.2 * ((maxDistance - distance) / maxDistance);
		score += 0.15 * article.getSource().getTrustworthiness();
		return score;
	}

	private double calculateDistance(double lon1, double lat1, double lon2,
			double lat2) {

		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double dlon = (Math.toRadians(lon2) - Math.toRadians(lon1));
		double dlat = (lat2 - lat1);

		double sinlat = Math.pow(Math.sin(dlat / 2), 2);
		double sinlon = Math.pow(Math.sin(dlon / 2), 2);

		double a = sinlat + Math.cos(lat1) * Math.cos(lat2) * sinlon;
		return 12742 * Math.asin(Math.min(1.0, Math.sqrt(a)));

	}

	private Collection<Long> getUnratedArticles() {
		Collection<Rating> ratings = rdao.getRatingsFromUser(user_id);
		Collection<String> ratedArticles = new ArrayDeque<String>(
				ratings.size());
		for (Rating rating : ratings) {
			ratedArticles.add(rating.getItem_id());
		}
		Collection<Article> unratedArticles = adao
				.getArticlesNotIn(ratedArticles);
		Collection<Long> unratedArticlesIDs = new ArrayDeque<Long>(
				unratedArticles.size());
		for (Article article : unratedArticles) {
			unratedArticlesIDs.add(Long.parseLong(model.fromIdToLong(
					article.getItem_id(), false)));
		}
		return unratedArticlesIDs;
	}

	public Collection<Rating> getMostRecent(int n) throws TasteException {
		List<Rating> ratings = getRatings();
		if (n >= ratings.size()) {
			return ratings;
		}
		List<Rating> nRatings = new ArrayList<Rating>(n);
		for (int i = 0; i < n; i++) {
			nRatings.add(ratings.get(i));
		}
		return nRatings;
	}

	private List<Rating> getMostRecent(long timestamp, long threshold)
			throws TasteException {
		List<Rating> nRatings = new LinkedList<Rating>();
		for (Rating rating : getRatings()) {
			if (rating.getCreated_at() < timestamp + threshold
					&& rating.getCreated_at() > timestamp - threshold) {
				nRatings.add(rating);
			}
		}
		return nRatings;
	}

	public Map<String, Double> getMostValuedGenres(Collection<Rating> ratings,
			double minScore) {
		Map<String, Double> entities = new HashMap<String, Double>();
		ArticleDAO adao = new ArticleDAO();
		double totalScore = 0.0;
		for (Rating rating : ratings) {
			if (rating.getScore() >= minScore) {
				Article article = adao.getArticle(rating.getItem_id());
				totalScore += rating.getScore() * article.getEntities().length;
				addGenres(entities, article, rating);
			}
		}
		normalizeMap(entities, totalScore);
		return orderMap(entities);
	}

	private void addGenres(Map<String, Double> entities, Article article,
			Rating rating) {
		for (String entity : article.getEntities()) {
			if (entities.containsKey(entity)) {
				entities.put(entity, entities.get(entity) + rating.getScore());
			} else {
				entities.put(entity, rating.getScore());
			}
		}
	}

	private void normalizeMap(Map<String, Double> genres, double totalScore) {
		totalScore /= 100;
		for (Map.Entry<String, Double> entry : genres.entrySet()) {
			entry.setValue(entry.getValue() / totalScore);
		}
	}

	private Map<String, Double> orderMap(Map<String, Double> genres) {
		Map<String, Double> sortedGenres = new TreeMap<String, Double>(
				new ValueComparatorDesc<String, Double>(genres));
		sortedGenres.putAll(genres);
		return sortedGenres;
	}

	private List<Rating> getRatings() throws TasteException {
		List<Rating> ratings = new LinkedList<Rating>();
		try {
			int uid = Integer.parseInt(model.fromIdToLong(user_id, true));
			LongPrimitiveIterator i = model.getItemIDsFromUser(uid).iterator();
			RatingDAO rdao = new RatingDAO();
			while (i.hasNext()) {
				ratings.add(rdao.getRating(user_id, model.fromLongToId(i.next())));
			}
			Collections.sort(ratings, TIME_ORDER);
		} catch (NoSuchUserException e) {
			
		}
		return ratings;
	}

}
