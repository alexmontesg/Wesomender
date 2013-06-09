package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.CachingUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.UserBasedBooleanSerializer;
import es.weso.amg.recommender.serializer.UserBasedSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a
 * {@link GenericUserBasedRecommender} or a
 * {@link GenericBooleanPrefUserBasedRecommender} depending on the result of
 * {@link DataModel#hasPreferenceValues()}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 20/12/2012
 * 
 */
public class UserBasedEvaluator extends AbstractEvaluator {

	/**
	 * Auxiliary class that stores pairs of a {@link UserNeighborhood} and the
	 * value of the parameter (threshold or nearest n) that the
	 * {@link UserNeighborhood} took
	 * 
	 * @author Alejandro Montes García <alejandro.montes@weso.es>
	 * @since 21/1/2013
	 * 
	 */
	private class NeighborhoodDuo {
		public UserNeighborhood nbhood;
		public Object param;

		public NeighborhoodDuo(UserNeighborhood nbhood, Object param) {
			this.nbhood = nbhood;
			this.param = param;
		}
	}

	private UserSimilarity currentSimilarity;
	private NeighborhoodDuo currentNbhood;

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		UserNeighborhood n = new CachingUserNeighborhood(currentNbhood.nbhood,
				model);
		UserSimilarity s = new CachingUserSimilarity(currentSimilarity, model);
		if (model.hasPreferenceValues()) {
			return new GenericUserBasedRecommender(model, n, s);
		} else {
			return new GenericBooleanPrefUserBasedRecommender(model, n, s);
		}
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		for (UserSimilarity similarity : getUserSimilarities()) {
			currentSimilarity = similarity;
			for (NeighborhoodDuo neighborhood : getNeighborhoods()) {
				currentNbhood = neighborhood;
				try {
					infos.add(runSingleEvaluation(getSerializer()));
				} catch (TasteException e) {
					logError(e, "Cannot run " + getName()
							+ ". Skipping its evaluation");
				}
			}
		}
		return infos;
	}

	@Override
	protected String getName() {
		return "GenericUserBased evaluation with similarity "
				+ currentSimilarity.getClass().getSimpleName()
				+ " and neighborhood "
				+ currentNbhood.nbhood.getClass().getSimpleName() + "(param = "
				+ currentNbhood.param + ")";
	}

	@Override
	protected AbstractSerializer getSerializer() {
		if (model.hasPreferenceValues()) {
			return new UserBasedSerializer(currentSimilarity.getClass()
					.getName(), currentNbhood.nbhood.getClass().getName(),
					currentNbhood.param);
		}
		return new UserBasedBooleanSerializer(currentSimilarity.getClass()
				.getName(), currentNbhood.getClass().getName(),
				currentNbhood.param);
	}

	/**
	 * Gets all the possible {@link UserNeighborhood} implementations that will
	 * be used in the evaluation
	 * 
	 * @return All the possible {@link UserNeighborhood} implementations that
	 *         will be used in the evaluation
	 */
	private Collection<NeighborhoodDuo> getNeighborhoods() {
		Collection<NeighborhoodDuo> neighborhoods = new HashSet<NeighborhoodDuo>();
		neighborhoods.addAll(getNearestNUserNeighborhoods());
		neighborhoods.addAll(getThresholdUserNeighborhoods());
		return neighborhoods;
	}

	/**
	 * Gets all the possible {@link NearestNUserNeighborhood} instances that
	 * will be used in the evaluation
	 * 
	 * @return All the possible {@link NearestNUserNeighborhood} instances that
	 *         will be used in the evaluation
	 */
	private Collection<NeighborhoodDuo> getNearestNUserNeighborhoods() {
		Collection<NeighborhoodDuo> neighborhoods = new HashSet<NeighborhoodDuo>();
		try {
			int nUsers = model.getNumUsers();
			int[] nVals = { (int) (0.01 * nUsers), (int) (0.001 * nUsers), 1,
					2, 3, 4, 5 };
			for (int i = 0; i < nVals.length; i++) {
				try {
					if (nVals[i] > 0) {
						neighborhoods.add(new NeighborhoodDuo(
								new NearestNUserNeighborhood(nVals[i],
										currentSimilarity, model), nVals[i]));
					}
				} catch (TasteException e) {
					logError(e, "Cannot create NearestNUserNeighborhood with"
							+ " n = " + nVals[i] + ". Skipping its evaluation");
				}
			}
		} catch (TasteException e) {
			logError(e, "Cannot determine number of users in the model."
					+ " Skipping NearestNNieghoborhood evaluations");
		}
		return neighborhoods;
	}

	/**
	 * Gets all the possible {@link ThresholdUserNeighborhood} instances that
	 * will be used in the evaluation
	 * 
	 * @return All the possible {@link ThresholdUserNeighborhood} instances that
	 *         will be used in the evaluation
	 */
	private Collection<NeighborhoodDuo> getThresholdUserNeighborhoods() {
		Collection<NeighborhoodDuo> neighborhoods = new HashSet<NeighborhoodDuo>();
		for (double i = 0.1; i <= 1; i += 0.1) {
			neighborhoods.add(new NeighborhoodDuo(
					new ThresholdUserNeighborhood(i, currentSimilarity, model),
					i));
		}
		return neighborhoods;
	}

}
