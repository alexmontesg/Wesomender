package es.weso.amg.recommender;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;

import es.weso.amg.recommender.evaluator.Evaluator;
import es.weso.amg.recommender.model.Movie;
import es.weso.amg.recommender.model.Rating;
import es.weso.amg.recommender.persistence.DatabaseBuilder;
import es.weso.amg.recommender.persistence.MovieDAO;
import es.weso.amg.recommender.user.UserPreferences;

public class Main {

	public static class ER {
		public long recency;
		public double genres;
	}

	public static void main(String[] args) {
		// buildDataBase();
		try {
			// MongoDBDataModel model = new MongoDBDataModel("localhost", 27017,
			// "recommend", "rating", false, false, null);
			DataModel model = new FileDataModel(new File(
					"src/main/resources/u.data"));
			Evaluator evaluator = new Evaluator(model);
			evaluator.evaluate();
			evaluator.serializeBest("src/main/resources/evaluator.best");
			MovieDAO mdao = new MovieDAO();
			/*
			 * for (int i = 1; i <= 10; i++) { UserPreferences up = new
			 * UserPreferences("" + i, model,
			 * evaluator.getBestRecommender(model)); evaluateUser(mdao, i, up);
			 * }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Process finished");
	}

	protected static void evaluateUser(MovieDAO mdao, int i, UserPreferences up)
			throws TasteException {
		for (double j = 0; j <= 1; j += 0.1) {
			System.out.println((j * 100) + "% collaborative");
			Collection<Rating> ratings = up.getPreferences(j, 10);
			Map<String, Double> genres = up.getMostValuedGenres(ratings, 2.5);
			List<Rating> orderedRatings = new ArrayList<Rating>(ratings.size());
			orderedRatings.addAll(ratings);
			Collections.sort(orderedRatings);
			List<ER> ers = new ArrayList<ER>(10);
			for (int k = 0; k < 10; k++) {
				if (orderedRatings.size() > k) {
					ers.add(evaluateRating(mdao, genres, orderedRatings, k));
				}
			}
			calculateAVG(i, ers);
		}
	}

	protected static void calculateAVG(int i, List<ER> ers) {
		ER avg = new ER();
		for (ER er : ers) {
			avg.genres += er.genres;
			avg.recency += er.recency;
		}
		avg.genres /= 10;
		avg.recency /= 10;
		System.out.println(i + "\t" + avg.recency + "\t" + avg.genres);
	}

	protected static ER evaluateRating(MovieDAO mdao,
			Map<String, Double> genres, List<Rating> orderedRatings, int k) {
		Rating toEvaluate = orderedRatings.get(k);
		ER er = new ER();
		Movie movie = mdao.getMovie(toEvaluate.getItem_id());
		er.recency = System.currentTimeMillis() - movie.getCreated_at();
		for (String genre : movie.getGenres()) {
			if (genres.containsKey(genre)) {
				er.genres += genres.get(genre);
			}
		}
		return er;
	}

	private static void buildDataBase() {
		try {
			new DatabaseBuilder("src/main/resources/u.data",
					"src/main/resources/movies.dat", ",").buildDatabase();
			System.out.println("Database has been built succesfully");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
