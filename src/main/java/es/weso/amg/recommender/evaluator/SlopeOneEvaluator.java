package es.weso.amg.recommender.evaluator;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.SlopeOneSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a basic
 * {@link SlopeOneRecommender}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 08/01/2013
 * 
 */
public class SlopeOneEvaluator extends AbstractEvaluator {

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		return new SlopeOneRecommender(model);
	}

	@Override
	protected String getName() {
		return "SlopeOne evaluation";
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		return evaluateSingle();
	}

	@Override
	protected AbstractSerializer getSerializer() {
		return new SlopeOneSerializer();
	}
}
