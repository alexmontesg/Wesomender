package es.weso.amg.recommender.serializer;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link SlopeOneRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 18/01/2013
 * 
 */
public class SlopeOneSerializer extends AbstractSerializer {

	private static final long serialVersionUID = 6386402483410936478L;

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		return new SlopeOneRecommender(dataModel);
	}

}
