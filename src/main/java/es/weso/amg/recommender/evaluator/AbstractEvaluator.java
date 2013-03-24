package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;

/**
 * Abstract implementation of {@link IRecommenderEval} that partially implements
 * its functionality and provides some utility methods
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 10/01/2013
 * 
 */
public abstract class AbstractEvaluator implements IRecommenderEval {

	protected int nErrors;
	protected DataModel model;
	protected RecommenderEvaluator evaluator;
	protected Logger log;

	/**
	 * Initializes the number of errors and the log
	 */
	public AbstractEvaluator() {
		nErrors = 0;
		log = Logger.getLogger(this.getClass());
		log.info("Building object of type: " + this.getClass());
	}

	public final int evaluate(Collection<RecommenderInfo> recommenders) {
		log.info("Running evaluation for the " + this.getClass());
		recommenders.addAll(evaluateAll());
		log.info("Evaluation completed. " + nErrors
				+ " errors occurred during this evaluation");
		return nErrors;
	}

	public void setDataModel(DataModel model) {
		this.model = model;
	}

	public void setEvaluator(RecommenderEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	/**
	 * Gets an {@link AbstractSerializer} that can be used to build a
	 * {@link Recommender} to be evaluated
	 * 
	 * @return An {@link AbstractSerializer} that can be used to build a
	 *         {@link Recommender} to be evaluated
	 */
	protected abstract AbstractSerializer getSerializer();

	/**
	 * Runs a single evaluation over a {@link RecommenderBuilder}
	 * 
	 * @param builder
	 *            The builder of the recommender to be evaluated
	 * @return A {@link RecommenderInfo} object containing the result of the
	 *         evaluation and the builder that was evaluated
	 * @throws TasteException
	 *             If the evaluation cannot be performed
	 */
	protected final RecommenderInfo runSingleEvaluation(
			AbstractSerializer builder) throws TasteException {
		double score = evaluator.evaluate(builder, null, model, TDATA, UDATA);
		log.info(getName() + " finished successfully with an score of " + score);
		return new RecommenderInfo(builder, score);
	}

	/**
	 * Gets all the possible {@link UserSimilarity} implementations that will be
	 * used in the evaluation
	 * 
	 * @return All the possible {@link UserSimilarity} implementations that will
	 *         be used in the evaluation
	 */
	protected Collection<UserSimilarity> getUserSimilarities() {
		Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>();
		if (model.hasPreferenceValues()) {
			try {
				userSimilarities.add(new PearsonCorrelationSimilarity(model));
			} catch (TasteException e) {
				logSimilarityError(e, "PearsonCorrelationSimilarity");
			}
			try {
				userSimilarities.add(new EuclideanDistanceSimilarity(model));
			} catch (TasteException e) {
				logSimilarityError(e, "EuclideanDistanceSimilarity");
			}
			try {
				userSimilarities.add(new UncenteredCosineSimilarity(model));
			} catch (TasteException e) {
				logSimilarityError(e, "UncenteredCosineSimilarity");
			}
		}
		userSimilarities.add(new CityBlockSimilarity(model));
		userSimilarities.add(new LogLikelihoodSimilarity(model));
		userSimilarities.add(new SpearmanCorrelationSimilarity(model));
		userSimilarities.add(new TanimotoCoefficientSimilarity(model));
		return userSimilarities;
	}

	/**
	 * Gets all the possible {@link ItemSimilarity} implementations that will be
	 * used in the evaluation
	 * 
	 * @return All the possible {@link ItemSimilarity} implementations that will
	 *         be used in the evaluation
	 */
	protected Collection<ItemSimilarity> getItemSimilarities() {
		Collection<ItemSimilarity> itemSimilarities = new HashSet<ItemSimilarity>();
		for (UserSimilarity similarity : getUserSimilarities()) {
			if (similarity instanceof ItemSimilarity) {
				itemSimilarities.add((ItemSimilarity) similarity);
			}
		}
		return itemSimilarities;
	}

	/**
	 * Increments the error counter and logs an error
	 * 
	 * @param exception
	 *            The {@link TasteException} that caused the error
	 * @param message
	 *            The message to be logged
	 */
	protected void logError(TasteException exception, String message) {
		nErrors++;
		log.error(message, exception);
	}

	/**
	 * Logs an error that occurred when creating a similarity
	 * 
	 * @param e
	 *            The {@link TasteException} that caused the error
	 * @param similarityName
	 *            The name of the similarity that could not been built
	 */
	protected void logSimilarityError(TasteException e, String similarityName) {
		logError(e, "Cannot create " + similarityName
				+ ". Skipping its evaluation");
	}

	/**
	 * Gets a specific implementation of a {@link Recommender}, parameterised as
	 * its class permits
	 * 
	 * @param model
	 *            The {@link DataModel} over which the {@link Recommender} will
	 *            be applied
	 * @return A {@link Recommender} of a specific class
	 * @throws TasteException
	 *             If the recommender cannot be built
	 */
	protected abstract Recommender getRecommender(DataModel model)
			throws TasteException;

	/**
	 * Gets the name of a specific {@link Recommender} class or object
	 * 
	 * @return The name of a specific {@link Recommender} class or object
	 */
	protected abstract String getName();

	/**
	 * Gets all the evaluation results of a specific {@link Recommender}
	 * implementation, that is, a {@link RecommenderInfo} for every single value
	 * of the parameters that can be applied to the {@link Recommender}
	 * 
	 * @return All the results for a {@link Recommender} implementation
	 */
	protected abstract Collection<RecommenderInfo> evaluateAll();

	/**
	 * Possible implementation of the {@link #evaluateAll()} method, it can be
	 * called when the {@link Recommender} to be evaluated can only be
	 * instantiated on a single way and therefore only one evaluation is run
	 * 
	 * @return The result of the single evaluation
	 */
	protected final Collection<RecommenderInfo> evaluateSingle() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		try {
			infos.add(runSingleEvaluation(getSerializer()));
		} catch (TasteException e) {
			logError(e, "Cannot run " + getName() + ". Skipping its evaluation");
		}
		return infos;
	}
}
