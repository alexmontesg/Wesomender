package es.weso.amg.recommender.evaluator;

import java.util.Collection;

import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.model.DataModel;

import es.weso.amg.recommender.model.RecommenderInfo;

/**
 * Interface to be implemented for all the evaluators that are wanted to be run
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 08/01/2013
 * 
 * @see AbstractEvaluator
 * 
 */
public interface IRecommenderEval {
	static final double TDATA = 0.5;
	static final double UDATA = 1.0;

	/**
	 * Performs an evaluation over a set of {@link Recommender}s. It adds the
	 * resulting {@link RecommenderInfo}s to the collection passed as a
	 * parameter
	 * 
	 * @param recommenders
	 *            The collection where the results of the evaluation will be
	 *            added
	 * @return The number of errors that occurred during the evaluation
	 */
	public int evaluate(Collection<RecommenderInfo> recommenders);

	/**
	 * Sets the {@link DataModel} to perform the evaluation over it
	 * 
	 * @param model
	 *            The model to perform the evaluation over it
	 */
	public void setDataModel(DataModel model);

	/**
	 * Sets the instance of {@link RecommenderEvaluator} that is going to be
	 * used during the evaluation
	 * 
	 * @param evaluator
	 *            The evaluator that will be used
	 */
	public void setEvaluator(RecommenderEvaluator evaluator);
}
