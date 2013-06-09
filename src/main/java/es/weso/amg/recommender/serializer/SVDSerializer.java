package es.weso.amg.recommender.serializer;

import java.lang.reflect.Constructor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Implementation of {@link AbstractSerializer} to serialize
 * {@link SVDRecommender}s
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 18/01/2013
 * 
 */
public class SVDSerializer extends AbstractSerializer {

	private static final long serialVersionUID = -6439933923637298956L;
	public String factorizerClassName;
	public Class<?>[] factorizerParamsType;
	public Object[] factorizerParamsValue;

	/**
	 * Creates a {@link KnnItemBasedSerializer} with the specified
	 * {@link Factorizer} class name, and its parameters
	 * 
	 * @param factorizerClassName
	 *            The fully qualified {@link Factorizer} class name
	 * @param factorizerParamsType
	 *            The fully qualified class name of the types that the
	 *            {@link Factorizer} takes as parameter in its constructor. They
	 *            have to be ordered as they appear in the signature of the
	 *            constructor
	 * @param factorizerParamsValue
	 *            The value of the attributes that will be passed to the
	 *            {@link Factorizer} constructor
	 */
	public SVDSerializer(String factorizerClassName,
			Class<?>[] factorizerParamsType, Object[] factorizerParamsValue) {
		this.factorizerClassName = factorizerClassName;
		this.factorizerParamsType = factorizerParamsType;
		this.factorizerParamsValue = factorizerParamsValue;
	}

	public Recommender buildRecommender(DataModel dataModel)
			throws TasteException {
		try {
			return new SVDRecommender(dataModel, buildFactorizer(dataModel));
		} catch (ReflectiveOperationException e) {
			log.error(e.getMessage());
			throw new TasteException(e.getMessage(), e);
		}
	}

	/**
	 * Reflectively builds a {@link Factorizer} with the provided name and
	 * parameters for a specific {@link DataModel}
	 * 
	 * @param model
	 *            The {@link DataModel} in which the {@link Factorizer} will be
	 *            applied
	 * @return The {@link Factorizer} for the specified model
	 * @throws ReflectiveOperationException
	 *             If the {@link Factorizer} cannot be reflectively loaded
	 */
	@SuppressWarnings("unchecked")
	private Factorizer buildFactorizer(DataModel model)
			throws ReflectiveOperationException {
		Class<? extends Factorizer> clazz = (Class<? extends Factorizer>) Class
				.forName(factorizerClassName);
		Class<?>[] paramsType = new Class[factorizerParamsType.length + 1];
		paramsType[0] = DataModel.class;
		int i = 1;
		for (Class<?> paramClazz : factorizerParamsType) {
			paramsType[i++] = paramClazz;
		}
		Object[] paramsValue = new Object[paramsType.length];
		paramsValue[0] = model;
		i = 1;
		for (Object value : factorizerParamsValue) {
			paramsValue[i++] = value;
		}
		Constructor<? extends Factorizer> c = clazz.getConstructor(paramsType);
		return c.newInstance(paramsValue);
	}

}
