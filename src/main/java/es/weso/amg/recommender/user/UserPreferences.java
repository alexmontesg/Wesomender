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

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import es.weso.amg.recommender.model.Movie;
import es.weso.amg.recommender.model.Rating;
import es.weso.amg.recommender.persistence.MovieDAO;
import es.weso.amg.recommender.persistence.RatingDAO;
import es.weso.amg.recommender.util.ValueComparatorDesc;

public class UserPreferences {

	private String user_id;
	private MongoDBDataModel model;
	private Recommender recommender;
	private MovieDAO mdao;
	private RatingDAO rdao;

	private static final Comparator<Rating> TIME_ORDER = new Comparator<Rating>() {
		public int compare(Rating r1, Rating r2) {
			if (r1.getCreated_at() == r2.getCreated_at()) {
				return r2.compareTo(r1);
			}
			return (r1.getCreated_at() < r2.getCreated_at() ? 1 : -1);
		}
	};

	public UserPreferences(String user_id, MongoDBDataModel model,
			Recommender recommender) {
		this.user_id = user_id;
		this.model = model;
		this.recommender = recommender;
		mdao = new MovieDAO();
		rdao = new RatingDAO();
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
		Collection<Long> unratedMovies = getUnratedMovies();

		Collection<Rating> estimatedRatings = new ArrayDeque<Rating>(
				unratedMovies.size());
		long long_user_id = Long.parseLong(model.fromIdToLong(user_id, true));
		Collection<String> haveCollaborativeScore = getHybridPreferences(
				collaborativeFiltering, genres, contentBased, unratedMovies,
				estimatedRatings, long_user_id);

		getContentBasedPreferences(genres, contentBased, estimatedRatings,
				long_user_id, haveCollaborativeScore);

		return estimatedRatings;
	}

	protected Collection<String> getHybridPreferences(
			double collaborativeFiltering, Map<String, Double> genres,
			double contentBased, Collection<Long> unratedMovies,
			Collection<Rating> estimatedRatings, long long_user_id)
			throws TasteException {
		List<RecommendedItem> recommendedItems = recommender.recommend(
				long_user_id, unratedMovies.size());
		Collection<String> haveCollaborativeScore = new ArrayDeque<String>(
				recommendedItems.size());
		for (RecommendedItem item : recommendedItems) {
			float collaborativeScore = item.getValue();
			String item_id = model.fromLongToId(item.getItemID());
			haveCollaborativeScore.add(item_id);
			double score = collaborativeFiltering * collaborativeScore
					+ contentBased
					* getContentBasedRating(genres, mdao.getMovie(item_id));
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

		Collection<Movie> dontHaveCollaborativeScore = mdao
				.getMoviesNotIn(haveCollaborativeScore);

		for (Movie movie : dontHaveCollaborativeScore) {
			double score = contentBased * getContentBasedRating(genres, movie);
			estimatedRatings.add(new Rating(user_id, movie.getItem_id(), score,
					System.currentTimeMillis()));
		}
	}

	private double getContentBasedRating(Map<String, Double> valuedGenres,
			Movie movie) {
		double score = 0.0;
		for (Map.Entry<String, Double> entry : valuedGenres.entrySet()) {
			if (movie.isOfGenre(entry.getKey())) {
				score += entry.getValue() / 100;
			}
		}
		double timeScore = 1.0
				- (System.currentTimeMillis() - movie.getCreated_at())
				/ (double) System.currentTimeMillis();
		score *= timeScore;
		return score;
	}

	private Collection<Long> getUnratedMovies() {
		Collection<Rating> ratings = rdao.getRatingsFromUser(user_id);
		Collection<String> ratedMovies = new ArrayDeque<String>(ratings.size());
		for (Rating rating : ratings) {
			ratedMovies.add(rating.getItem_id());
		}
		Collection<Movie> unratedMovies = mdao.getMoviesNotIn(ratedMovies);
		Collection<Long> unratedMoviesIds = new ArrayDeque<Long>(
				unratedMovies.size());
		for (Movie movie : unratedMovies) {
			unratedMoviesIds.add(Long.parseLong(model.fromIdToLong(
					movie.getItem_id(), false)));
		}
		return unratedMoviesIds;
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
		Map<String, Double> genres = new HashMap<String, Double>();
		MovieDAO mdao = new MovieDAO();
		double totalScore = 0.0;
		for (Rating rating : ratings) {
			if (rating.getScore() >= minScore) {
				Movie movie = mdao.getMovie(rating.getItem_id());
				totalScore += rating.getScore() * movie.getGenres().length;
				addGenres(genres, movie, rating);
			}
		}
		normalizeMap(genres, totalScore);
		return orderMap(genres);
	}

	private void addGenres(Map<String, Double> genres, Movie movie,
			Rating rating) {
		for (String genre : movie.getGenres()) {
			if (genres.containsKey(genre)) {
				genres.put(genre, genres.get(genre) + rating.getScore());
			} else {
				genres.put(genre, rating.getScore());
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
		int uid = Integer.parseInt(model.fromIdToLong(user_id, true));
		LongPrimitiveIterator i = model.getItemIDsFromUser(uid).iterator();
		List<Rating> ratings = new LinkedList<Rating>();
		RatingDAO rdao = new RatingDAO();
		while (i.hasNext()) {
			ratings.add(rdao.getRating(user_id, model.fromLongToId(i.next())));
		}
		Collections.sort(ratings, TIME_ORDER);
		return ratings;
	}

}
