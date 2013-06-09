package es.weso.amg.recommender.serializer;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link KnnItemBasedRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 21/01/2013
 * 
 */
public class KnnItemBasedSerializer extends AbstractSerializer {

	private static final long serialVersionUID = -8975675951661648359L;
	private String optimizerClassName, similarityClassName;
	private int neighborhoodSize;

	/**
	 * Creates a {@link KnnItemBasedSerializer} with the specified
	 * {@link ItemSimilarity} class name, {@link Optimizer} class name and
	 * neighborhood size
	 * 
	 * @param optimizerClassName
	 *            The fully qualified {@link Optimizer} class name
	 * @param similarityClassName
	 *            The fully qualified {@link ItemSimilarity} class name
	 * @param neighborhoodSize
	 *            The neighborhood size
	 */
	public KnnItemBasedSerializer(String optimizerClassName,
			String similarityClassName, int neighborhoodSize) {
		this.optimizerClassName = optimizerClassName;
		this.similarityClassName = similarityClassName;
		this.neighborhoodSize = neighborhoodSize;
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new KnnItemBasedRecommender(dataModel,
					new CachingItemSimilarity(getItemSimilarity(dataModel,
							similarityClassName), dataModel), getOptimizer(),
					neighborhoodSize);
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

	/**
	 * Builds an {@link Optimizer} with the provided class name
	 * 
	 * @return The {@link Optimizer}
	 * @throws ReflectiveOperationException
	 *             If the {@link Optimizer} cannot be reflectively loaded
	 */
	@SuppressWarnings("unchecked")
	private Optimizer getOptimizer() throws ReflectiveOperationException {
		Class<? extends Optimizer> clazz = (Class<? extends Optimizer>) Class
				.forName(optimizerClassName);
		return clazz.newInstance();
	}

}
