package es.weso.amg.recommender.serializer;

import java.lang.reflect.Constructor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link TreeClusteringRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 18/01/2013
 * 
 */
public class TreeClusteringSerializer extends AbstractSerializer {

	private static final long serialVersionUID = 2813823456314141705L;
	private String clusterSimilarityClassName, userSimilarityClassName;
	private int numClusters;

	/**
	 * Creates a {@link TreeClusteringSerializer} with the specified
	 * {@link ClusterSimilarity} and {@link UserSimilarity} class names and
	 * amount of clusters
	 * 
	 * @param clusterSimilarityClassName
	 *            The fully qualified {@link ClusterSimilarity} class name
	 * @param userSimilarityClassName
	 *            The fully qualified {@link UserSimilarity} class name
	 * @param numClusters
	 *            The amount of clusters
	 */
	public TreeClusteringSerializer(String clusterSimilarityClassName,
			String userSimilarityClassName, int numClusters) {
		this.numClusters = numClusters;
		this.clusterSimilarityClassName = clusterSimilarityClassName;
		this.userSimilarityClassName = userSimilarityClassName;
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new TreeClusteringRecommender(dataModel,
					buildClusterSimilarity(dataModel), numClusters);
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

	/**
	 * Reflectively builds a {@link ClusterSimilarity} with the provided name
	 * and parameters for a specific {@link DataModel}
	 * 
	 * @param model
	 *            The {@link DataModel} in which the {@link ClusterSimilarity}
	 *            will be applied
	 * @return The {@link ClusterSimilarity} for the specified model
	 * @throws ReflectiveOperationException
	 *             If the {@link ClusterSimilarity} cannot be reflectively
	 *             loaded
	 * @throws TasteException
	 *             If there is an error accessing the model
	 * 
	 */
	@SuppressWarnings("unchecked")
	private ClusterSimilarity buildClusterSimilarity(DataModel model)
			throws ReflectiveOperationException, TasteException {
		Class<? extends ClusterSimilarity> clazz = (Class<? extends ClusterSimilarity>) Class
				.forName(clusterSimilarityClassName);
		Constructor<? extends ClusterSimilarity> c = clazz
				.getConstructor(UserSimilarity.class);
		Class<? extends UserSimilarity> usClazz = (Class<? extends UserSimilarity>) Class
				.forName(userSimilarityClassName);
		Constructor<? extends UserSimilarity> usc = usClazz
				.getConstructor(DataModel.class);
		return c.newInstance(new CachingUserSimilarity(usc.newInstance(model),
				model));
	}

}
