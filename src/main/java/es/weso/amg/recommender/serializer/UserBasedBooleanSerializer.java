package es.weso.amg.recommender.serializer;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.CachingUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Implementation of {@link UserBasedSerializer} to serialize
 * {@link GenericBooleanPrefUserBasedRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 21/01/2013
 * 
 */
public class UserBasedBooleanSerializer extends UserBasedSerializer {
	
	private static final long serialVersionUID = -7946230257325587096L;

	public UserBasedBooleanSerializer(String userSimilarityClassName,
			String neighborhoodClassName, Object neighborhoodParam) {
		super(userSimilarityClassName, neighborhoodClassName, neighborhoodParam);
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new GenericBooleanPrefUserBasedRecommender(dataModel,
					new CachingUserNeighborhood(getNeighborhood(dataModel),
							dataModel), new CachingUserSimilarity(
							getSimilarity(dataModel), dataModel));
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

}
