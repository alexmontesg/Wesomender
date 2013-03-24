package es.weso.amg.recommender.serializer;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link GenericBooleanPrefItemBasedRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 21/01/2013
 * 
 */
public class GenericBooleanPrefItemBasedSerializer extends AbstractSerializer {

	private static final long serialVersionUID = 7237566842116253743L;
	private String itemSimilarityName;

	/**
	 * Creates a {@link GenericBooleanPrefItemBasedSerializer} with the
	 * specified {@link ItemSimilarity} class name
	 * 
	 * @param itemSimilarityName
	 *            The fully qualified {@link ItemSimilarity} class name
	 */
	public GenericBooleanPrefItemBasedSerializer(String itemSimilarityName) {
		this.itemSimilarityName = itemSimilarityName;
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new GenericBooleanPrefItemBasedRecommender(dataModel,
					new CachingItemSimilarity(getItemSimilarity(dataModel,
							itemSimilarityName), dataModel));
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

}
