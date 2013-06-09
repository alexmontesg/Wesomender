package es.weso.amg.recommender.serializer;

import java.lang.reflect.Constructor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.CachingUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link GenericUserBasedRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 21/01/2013
 * 
 * @see UserBasedBooleanSerializer UserBasedBooleanSerializer for a serializer
 *      where the preference values are not taken into account
 */
public class UserBasedSerializer extends AbstractSerializer {

	private static final long serialVersionUID = 6683472837864735136L;
	protected String userSimilarityClassName;
	protected String neighborhoodClassName;
	protected Object neighborhoodParam;

	/**
	 * Creates a {@link UserBasedSerializer} with the specified
	 * {@link UserSimilarity} and {@link UserNeighborhood}
	 * 
	 * @param userSimilarityClassName
	 *            The fully qualified {@link UserSimilarity} class name
	 * @param neighborhoodClassName
	 *            The fully qualified {@link UserNeighborhood} class name
	 * @param neighborhoodParam
	 *            The parameter for the {@link UserNeighborhood}, it can be
	 *            either the threshold or amount of nearest users
	 */
	public UserBasedSerializer(String userSimilarityClassName,
			String neighborhoodClassName, Object neighborhoodParam) {
		this.userSimilarityClassName = userSimilarityClassName;
		this.neighborhoodClassName = neighborhoodClassName;
		this.neighborhoodParam = neighborhoodParam;
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new GenericUserBasedRecommender(dataModel,
					new CachingUserNeighborhood(getNeighborhood(dataModel),
							dataModel), new CachingUserSimilarity(
							getSimilarity(dataModel), dataModel));
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

	/**
	 * Reflectively builds a {@link UserNeighborhood} with the provided name and
	 * parameter for a specific {@link DataModel}
	 * 
	 * @param dataModel
	 *            The {@link DataModel} in which the {@link UserNeighborhood}
	 *            will be applied
	 * @return The {@link UserNeighborhood} for the specified model
	 * @throws ReflectiveOperationException
	 *             If the {@link UserNeighborhood} cannot be reflectively loaded
	 * @throws TasteException
	 *             If there is an error accessing the model
	 */
	@SuppressWarnings("unchecked")
	protected UserNeighborhood getNeighborhood(DataModel dataModel)
			throws ReflectiveOperationException, TasteException {
		Class<? extends UserNeighborhood> clazz = (Class<? extends UserNeighborhood>) Class
				.forName(neighborhoodClassName);
		Constructor<? extends UserNeighborhood> constructor;
		if (neighborhoodParam.getClass().equals(Double.class)) {
			constructor = clazz.getConstructor(double.class,
					UserSimilarity.class, DataModel.class);
		} else {
			constructor = clazz.getConstructor(int.class, UserSimilarity.class,
					DataModel.class);
		}
		return constructor.newInstance(neighborhoodParam,
				new CachingUserSimilarity(getSimilarity(dataModel), dataModel),
				dataModel);
	}

	/**
	 * Reflectively builds a {@link UserSimilarity} with the provided name and
	 * parameter for a specific {@link DataModel}
	 * 
	 * @param dataModel
	 *            The {@link DataModel} in which the {@link UserSimilarity}
	 *            will be applied
	 * @return The {@link UserSimilarity} for the specified model
	 * @throws ReflectiveOperationException
	 *             If the {@link UserSimilarity} cannot be reflectively loaded
	 */
	@SuppressWarnings("unchecked")
	protected UserSimilarity getSimilarity(DataModel dataModel)
			throws ReflectiveOperationException {
		Class<? extends UserSimilarity> clazz = (Class<? extends UserSimilarity>) Class
				.forName(userSimilarityClassName);
		Constructor<? extends UserSimilarity> constructor = clazz
				.getConstructor(DataModel.class);
		return constructor.newInstance(dataModel);
	}
}
