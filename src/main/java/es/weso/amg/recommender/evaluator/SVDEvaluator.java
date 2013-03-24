package es.weso.amg.recommender.evaluator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.ExpectationMaximizationSVDFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.ImplicitLinearRegressionFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

import es.weso.amg.recommender.model.RecommenderInfo;
import es.weso.amg.recommender.serializer.AbstractSerializer;
import es.weso.amg.recommender.serializer.SVDSerializer;

/**
 * Implementation of {@link IRecommenderEval} to evaluate a
 * {@link SVDRecommender}
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 11/01/2013
 * 
 */
public class SVDEvaluator extends AbstractEvaluator {

	/**
	 * Auxiliary class that stores pairs with the type and value of the
	 * {@link Factorizer} parameters
	 * 
	 * @author Alejandro Montes García <alejandro.montes@weso.es>
	 * @since 21/01/2013
	 * 
	 */
	private class ParamDuo {
		public Class<?> type;
		public Object value;

		public ParamDuo(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}
	}

	private Map.Entry<Factorizer, Collection<ParamDuo>> currentFactorizer;

	@Override
	protected Recommender getRecommender(DataModel model) throws TasteException {
		return new SVDRecommender(model, currentFactorizer.getKey());
	}

	@Override
	protected Collection<RecommenderInfo> evaluateAll() {
		Collection<RecommenderInfo> infos = new HashSet<RecommenderInfo>();
		for (Map.Entry<Factorizer, Collection<ParamDuo>> factorizer : getFactorizers()
				.entrySet()) {
			currentFactorizer = factorizer;
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
	protected String getName() {
		return "SVD evaluation with "
				+ currentFactorizer.getKey().getClass().getSimpleName();
	}

	@Override
	protected AbstractSerializer getSerializer() {
		Class<?>[] types = new Class<?>[currentFactorizer.getValue().size()];
		Object[] values = new Object[types.length];
		int i = 0;
		for (ParamDuo duo : currentFactorizer.getValue()) {
			types[i] = duo.type;
			values[i++] = duo.value;
		}
		return new SVDSerializer(currentFactorizer.getKey().getClass()
				.getName(), types, values);
	}

	/**
	 * Logs an error that occurred when creating a {@link Factorizer}
	 * 
	 * @param e
	 *            The exception that caused the error
	 * @param factorizerName
	 *            The name of the {@link Factorizer} that could not been built
	 */
	private void logFactorizerError(TasteException e, String factorizerName) {
		logError(e, "Unable to create" + factorizerName
				+ ". Skipping its evaluation");
	}

	/**
	 * Gets all the possible {@link Factorizer}s that can be used when building an
	 * {@link SVDRecommender}
	 * 
	 * @return All the possible {@link Factorizer}s that can be used when building an
	 *         {@link SVDRecommender}
	 */
	private Map<Factorizer, Collection<ParamDuo>> getFactorizers() {
		Map<Factorizer, Collection<ParamDuo>> factorizers = new HashMap<Factorizer, Collection<ParamDuo>>();
		Collection<ParamDuo> params;
		try {
			params = new LinkedList<ParamDuo>();
			params.add(new ParamDuo(int.class, 8));
			params.add(new ParamDuo(double.class, 0.065));
			params.add(new ParamDuo(int.class, 25));
			factorizers.put(new ALSWRFactorizer(model, 8, 0.065, 25), params);
		} catch (TasteException e) {
			logFactorizerError(e, "ALSWRFactorizer");
		}
		try {
			params = new LinkedList<ParamDuo>();
			params.add(new ParamDuo(int.class, 50));
			params.add(new ParamDuo(int.class, 25));
			factorizers.put(new ExpectationMaximizationSVDFactorizer(model, 50,
					25), params);
		} catch (TasteException e) {
			logFactorizerError(e, "ExpectationMaximizationSVDFactorizer");
		}
		try {
			params = new LinkedList<ParamDuo>();
			factorizers.put(new ImplicitLinearRegressionFactorizer(model),
					params);
		} catch (TasteException e) {
			logFactorizerError(e, "ImplicitLinearRegressionFactorizer");
		}
		return factorizers;
	}
}
