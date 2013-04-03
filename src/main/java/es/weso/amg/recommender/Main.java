package es.weso.amg.recommender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;

import es.weso.amg.recommender.model.Rating;
import es.weso.amg.recommender.persistence.ArticleDAO;
import es.weso.amg.recommender.persistence.DatabaseBuilder;
import es.weso.amg.recommender.user.UserPreferences;

public class Main {

	public static void main(String[] args) {
		buildDataBase();

		try {
			MongoDBDataModel model = new MongoDBDataModel("localhost", 27017,
					"recommend", "rating", false, false, null);
			// Evaluator evaluator = new Evaluator(model);
			// evaluator.evaluate();
			// evaluator.serializeBest("src/main/resources/evaluator.best");
			ArticleDAO adao = new ArticleDAO();

			for (int i = 1; i <= 10; i++) {
				UserPreferences up = new UserPreferences("" + i, model,
						new SlopeOneRecommender(model));
				evaluateUser(adao, i, up);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Process finished");
	}

	protected static void evaluateUser(ArticleDAO adao, int i,
			UserPreferences up) throws TasteException {
		for (double j = 0; j <= 1; j += 0.1) {
			Collection<Rating> ratings = up.getPreferences(j, 10);
			List<Rating> orderedRatings = new ArrayList<Rating>(ratings.size());
			orderedRatings.addAll(ratings);
			Collections.sort(orderedRatings);
			Collections.reverse(orderedRatings);
			System.out.println(orderedRatings);
		}
	}

	protected static void buildDataBase() {
		try {
			new DatabaseBuilder("src/main/resources/recs.data",
					"src/main/resources/news.data", ":").buildDatabase();
			System.out.println("Database has been built succesfully");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
