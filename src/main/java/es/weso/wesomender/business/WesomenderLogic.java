package es.weso.wesomender.business;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.recommender.Recommender;

import com.mongodb.MongoException;

import es.weso.amg.recommender.evaluator.Evaluator;
import es.weso.amg.recommender.model.Rating;
import es.weso.amg.recommender.persistence.RatingDAO;
import es.weso.amg.recommender.user.UserPreferences;

public class WesomenderLogic {

	private RatingDAO rdao;
	private static WesomenderLogic instance;
	private MongoDBDataModel model;
	private static Logger log = Logger.getLogger(WesomenderLogic.class);
	private static final int PORT = 27017;
	private static final String HOST = "localhost";
	private static final String DBNAME = "recommend";
	private static final String COLNAME = "rating";

	private WesomenderLogic() throws UnknownHostException, MongoException {
		rdao = new RatingDAO();
		model = new MongoDBDataModel(HOST, PORT, DBNAME, COLNAME, false, false,
				null);
	}

	public static WesomenderLogic getInstance() throws UnknownHostException, MongoException {
		if (instance == null) {
			instance = new WesomenderLogic();
		}
		return instance;
	}

	public void rate(String articleId, String userId, Double score) {
		Rating r = new Rating();
		r.setItem_id(articleId);
		r.setScore(score);
		r.setUser_id(userId);
		r.setCreated_at(new Date().getTime());
		rdao.add(r);
	}

	public Collection<String> recommend(String userId, double lat, double lon)
			throws UnknownHostException, TasteException {
		Recommender recommender = null;
		try {
			recommender = Evaluator.deserialize("best", model);
		} catch (Exception e) {
			recommender = new SlopeOneRecommender(model);
		}
		Collection<Rating> prefs = new UserPreferences(userId, model,
				recommender, lat, lon).getPreferences(0.5, 20);
		List<Rating> orderedRatings = new ArrayList<Rating>(prefs.size());
		orderedRatings.addAll(prefs);
		Collections.sort(orderedRatings);
		Collections.reverse(orderedRatings);
		Collection<String> ids = new ArrayDeque<String>(orderedRatings.size());
		for (Rating r : orderedRatings) {
			ids.add(r.getItem_id());
		}
		return ids;
	}
	
	public void evaluate() {
		new EvaluateThread().start();
	}
	
	private class EvaluateThread extends Thread {

		public void run() {
			try {
				model = new MongoDBDataModel(HOST, PORT,
						DBNAME, COLNAME, false, false, null);
				Evaluator eval = new Evaluator(model);
				eval.evaluate();
				eval.serializeBest("best");
			} catch (UnknownHostException e) {
				log.error(e.getMessage(), e);
			} catch (MongoException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalStateException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}

	}

}
