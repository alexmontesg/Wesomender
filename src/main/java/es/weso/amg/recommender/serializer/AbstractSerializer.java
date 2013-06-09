package es.weso.amg.recommender.serializer;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Abstract representation of a {@link RecommenderBuilder} that can be
 * serialized
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 18/01/2013
 * 
 * @see Serializable
 * 
 */
public abstract class AbstractSerializer implements RecommenderBuilder,
		Serializable {

	transient protected Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = -5196573405655912447L;

	/**
	 * Reflectively builds a {@link ItemSimilarity} with the provided name for a
	 * specific {@link DataModel}
	 * 
	 * @param model
	 *            The {@link DataModel} in which the similarity will be applied
	 * @param similarityClassName
	 *            The name of the {@link ItemSimilarity} class
	 * @return The {@link ItemSimilarity} for the specified model
	 * @throws ReflectiveOperationException
	 *             If the {@link ItemSimilarity} cannot be reflectively loaded
	 */
	@SuppressWarnings("unchecked")
	protected ItemSimilarity getItemSimilarity(DataModel model,
			String similarityClassName) throws ReflectiveOperationException {
		Class<? extends ItemSimilarity> clazz = (Class<? extends ItemSimilarity>) Class
				.forName(similarityClassName);
		Constructor<? extends ItemSimilarity> constructor = clazz
				.getConstructor(DataModel.class);
		return constructor.newInstance(model);
	}

}
