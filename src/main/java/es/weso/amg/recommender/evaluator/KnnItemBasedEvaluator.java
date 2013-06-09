package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.knn.ConjugateGradientOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.NonNegativeQuadraticOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.KnnItemBasedSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a
 * {@link KnnItemBasedRecommender}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 17/01/2013
 * 
 */
public class KnnItemBasedEvaluator extends AbstractEvaluator {

	private ItemSimilarity currentSimilarity;
	private int currentNeighborhoodSize;
	private Optimizer currentOptimizer;

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		return new KnnItemBasedRecommender(model, new CachingItemSimilarity(
				currentSimilarity, model), currentOptimizer,
				currentNeighborhoodSize);
	}

	@Override
	protected String getName() {
		return "KnnItemBasedRecommender with similarity "
				+ currentSimilarity.getClass().getSimpleName() + " optimizer "
				+ currentOptimizer.getClass().getSimpleName()
				+ " and neighborhood size " + currentNeighborhoodSize;
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		for (ItemSimilarity similarity : getItemSimilarities()) {
			currentSimilarity = similarity;
			for (int neighborhoodSize : getNeighborhoodSizes()) {
				currentNeighborhoodSize = neighborhoodSize;
				for (Optimizer optimizer : getOptimizers()) {
					currentOptimizer = optimizer;
					try {
						infos.add(runSingleEvaluation(getSerializer()));
					} catch (TasteException e) {
						logError(e, "Cannot run " + getName()
								+ ". Skipping its evaluation");
					}
				}

			}
		}
		return infos;
	}

	@Override
	protected AbstractSerializer getSerializer() {
		return new KnnItemBasedSerializer(
				currentOptimizer.getClass().getName(), currentSimilarity
						.getClass().getName(), currentNeighborhoodSize);
	}

	/**
	 * Gets all the possible neighborhood sizes that will be used in the
	 * evaluation
	 * 
	 * @return All the possible neighborhood sizes that will be used in the
	 *         evaluation
	 */
	private Collection<Integer> getNeighborhoodSizes() {
		Collection<Integer> sizes = new HashSet<Integer>();
		try {
			sizes.add((int) (0.01 * model.getNumItems()));
		} catch (TasteException e) {
			logError(e, "Cannot determine the number of items");
		}
		try {
			sizes.add((int) (0.001 * model.getNumItems()));
		} catch (TasteException e) {
			logError(e, "Cannot determine the number of items");
		}
		sizes.add(2);
		sizes.add(3);
		sizes.add(4);
		sizes.add(5);
		sizes.remove(0);
		return sizes;
	}

	/**
	 * Gets all the possible {@link Optimizer} implementations that will be used
	 * in the evaluation
	 * 
	 * @return All the possible {@link Optimizer} implementations that will be
	 *         used in the evaluation
	 */
	private Collection<Optimizer> getOptimizers() {
		Collection<Optimizer> optimizers = new HashSet<Optimizer>();
		optimizers.add(new ConjugateGradientOptimizer());
		optimizers.add(new NonNegativeQuadraticOptimizer());
		return optimizers;
	}

}
