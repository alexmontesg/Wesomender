package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.FarthestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.NearestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.TreeClusteringSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a basic
 * {@link TreeClusteringRecommender}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 15/01/2013
 * 
 */
public class TreeClusteringEvaluator extends AbstractEvaluator {

	/**
	 * Auxiliary class to store pairs of {@link ClusterSimilarity} and their
	 * {@link UserSimilarity} implementations
	 * 
	 * @author Alejandro Montes García <alejandro.montes@weso.es>
	 * @since 15/01/2013
	 * 
	 */
	private class SimilarityDuo {
		public UserSimilarity user;
		public ClusterSimilarity cluster;

		public SimilarityDuo(UserSimilarity user, ClusterSimilarity cluster) {
			this.user = user;
			this.cluster = cluster;
		}
	}

	private int currentNumClusters;
	private SimilarityDuo currentSimilarity;

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		return new TreeClusteringRecommender(model, currentSimilarity.cluster,
				currentNumClusters);
	}

	@Override
	protected String getName() {
		return "TreeClustering evaluation with " + currentNumClusters
				+ " clusters and "
				+ currentSimilarity.cluster.getClass().getSimpleName() + "("
				+ currentSimilarity.user.getClass().getSimpleName()
				+ ") similarity";
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		for (int numClusters : getAllNumClusters()) {
			currentNumClusters = numClusters;
			for (SimilarityDuo similarity : getAllSimilarities()) {
				currentSimilarity = similarity;
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
	protected AbstractSerializer getSerializer() {
		return new TreeClusteringSerializer(currentSimilarity.cluster
				.getClass().getName(), currentSimilarity.user.getClass()
				.getName(), currentNumClusters);
	}

	/**
	 * Gets all the possible {@link SimilarityDuo}s that can be used when
	 * building a {@link TreeClusteringRecommender}
	 * 
	 * @return All the possible {@link SimilarityDuo}s that can be used when
	 *         building a {@link TreeClusteringRecommender}
	 */
	private Collection<SimilarityDuo> getAllSimilarities() {
		Collection<SimilarityDuo> similarities = new HashSet<SimilarityDuo>();
		for (UserSimilarity similarity : getUserSimilarities()) {
			try {
				similarities.add(new SimilarityDuo(similarity,
						new FarthestNeighborClusterSimilarity(
								new CachingUserSimilarity(similarity, model))));
			} catch (TasteException e) {
				logSimilarityError(e, "FarthestNeighborClusterSimilarity with "
						+ similarity.getClass().getSimpleName());
			}
			try {
				similarities.add(new SimilarityDuo(similarity,
						new NearestNeighborClusterSimilarity(
								new CachingUserSimilarity(similarity, model))));
			} catch (TasteException e) {
				logSimilarityError(e, "NearestNeighborClusterSimilarity with "
						+ similarity.getClass().getSimpleName());
			}
		}
		return similarities;
	}

	/**
	 * Gets all the possible amount of clusters that can be used when building a
	 * {@link TreeClusteringRecommender}
	 * 
	 * @return All the possible amount of clusters that can be used when
	 *         building an {@link TreeClusteringRecommender}
	 */
	private Collection<Integer> getAllNumClusters() {
		Collection<Integer> clusters = new HashSet<Integer>();
		try {
			int numUsers = model.getNumUsers();
			clusters.add((int) (numUsers * 0.5));
			clusters.add((int) (numUsers * 0.25));
			clusters.add((int) (numUsers * 0.12));
			clusters.add((int) (numUsers * 0.05));
		} catch (TasteException e) {
			logError(e, "Cannot get the number of users. "
					+ "Setting default values for the amount of clusters.");
		}
		for (int i : clusters) {
			if (i < 2) {
				clusters.remove(i);
			}
		}
		if (clusters.isEmpty()) {
			clusters.add(50);
			clusters.add(100);
			clusters.add(200);
			clusters.add(500);
		}
		return clusters;
	}

}
