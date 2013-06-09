package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashSet;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.GenericBooleanPrefItemBasedSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a
 * {@link GenericBooleanPrefItemBasedRecommender}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 17/01/2013
 * 
 */
public class GenericBooleanPrefItemBasedEvaluator extends AbstractEvaluator {

	private ItemSimilarity currentSimilarity;

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		return new GenericBooleanPrefItemBasedRecommender(model,
				new CachingItemSimilarity(currentSimilarity, model));
	}

	@Override
	protected String getName() {
		return "GenericBooleanPrefItemBased with item similarity = "
				+ currentSimilarity.getClass().getSimpleName();
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		for (ItemSimilarity similarity : getItemSimilarities()) {
			currentSimilarity = similarity;
			try {
				infos.add(runSingleEvaluation(getSerializer()));
			} catch (TasteException e) {
				logError(e, "Cannot run " + getName()
						+ ". Skipping its evaluation");
			}
		}
		return infos;
	}

	@Override
	protected AbstractSerializer getSerializer() {
		return new GenericBooleanPrefItemBasedSerializer(currentSimilarity
				.getClass().getName());
	}

}
