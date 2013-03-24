package es.weso.amg.recommender.evaluator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import es.weso.amg.recommender.model.RecommenderInfo;

/**
 * Class that provides the interface between the system and the outside of the
 * system
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 20/12/2012
 * 
 */
public class Evaluator {

	private Collection<RecommenderInfo> recommenders;
	private DataModel model;
	private RecommenderEvaluator evaluator;
	private Logger log;

	/**
	 * Sets the {@link DataModel} and the {@link RecommenderEvaluator} to be
	 * used
	 * 
	 * @param model
	 *            The {@link DataModel} to be used
	 */
	public Evaluator(DataModel model) {
		this.model = model;
		evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		recommenders = new TreeSet<RecommenderInfo>();
		log = Logger.getLogger(this.getClass());
	}

	/**
	 * Gets the best {@link Recommender} of the in-memory evaluation
	 * 
	 * @param model
	 *            The model in which the {@link Recommender} will be applied
	 * @return The most accurate {@link Recommender} of the evaluation
	 * @throws IllegalStateException
	 *             If there is no results of a evaluation, {@link #evaluate()}
	 *             should be called first
	 * @throws TasteException
	 *             If the {@link Recommender} cannot be built
	 */
	public Recommender getBestRecommender(DataModel model)
			throws IllegalStateException, TasteException {
		return getBestInfo().getAbstractSerializer().buildRecommender(model);
	}

	/**
	 * Runs all the available {@link IRecommenderEval} and stores the results in
	 * a collection, deleting the previous results. It is, therefore, equivalent
	 * to {@link #evaluate(boolean) evaluate(true)}
	 */
	public void evaluate() {
		evaluate(true);
	}

	/**
	 * Runs all the available {@link IRecommenderEval} and stores the results in
	 * a collection
	 * 
	 * @param dropOld
	 *            If the previous results in the collection have to be deleted
	 */
	public void evaluate(boolean dropOld) {
		if (dropOld) {
			log.info("Removing old recommenders");
			recommenders.clear();
		}
		int nErrors = 0;
		for (IRecommenderEval evaluator : getAvailableEvaluators()) {
			nErrors += evaluator.evaluate(recommenders);
		}
		if (nErrors > 0) {
			log.warn(nErrors + " errors occured during evaluation."
					+ " Some recommenders might have not been checked.");
		} else {
			log.info("Evaluation concluded successfully");
		}
		deleteNaNs();
	}

	/**
	 * Serializes the best {@link RecommenderInfo} to a specified path
	 * 
	 * @param path
	 *            The path where the {@link RecommenderInfo} will be serialized
	 * @throws IllegalStateException
	 *             If there is no {@link RecommenderInfo} ready to be
	 *             serialized, {@link #evaluate()} should be called first
	 * @throws IOException
	 *             If there is an error serializing the {@link RecommenderInfo}
	 */
	public void serializeBest(String path) throws IllegalStateException,
			IOException {
		FileOutputStream fileOut = new FileOutputStream(path);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(getBestInfo());
		out.close();
		fileOut.close();
	}

	/**
	 * Deserializes a {@link RecommenderInfo}
	 * 
	 * @param path
	 *            The path where the {@link RecommenderInfo} is serialized
	 * @param model
	 *            The {@link DataModel} in which the {@link RecommenderInfo}
	 *            will be applied
	 * @return The {@link Recommender} built with the specified model and the
	 *         {@link RecommenderInfo} serialized in the specified path
	 * @throws IOException
	 *             If there is an error reading the object
	 * @throws ClassNotFoundException
	 *             If the class of the serialized object cannot be found
	 * @throws TasteException
	 *             If the {@link Recommender} cannot be built
	 */
	public static Recommender deserialize(String path, DataModel model)
			throws IOException, ClassNotFoundException, TasteException {
		FileInputStream fileIn = new FileInputStream(path);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		RecommenderInfo recommender = (RecommenderInfo) in.readObject();
		in.close();
		fileIn.close();
		return new CachingRecommender(recommender.getAbstractSerializer()
				.buildRecommender(model));
	}

	/**
	 * Gets the best {@link RecommenderInfo} in the collection of results
	 * 
	 * @return The best {@link RecommenderInfo} in the collection of results
	 * @throws IllegalStateException
	 *             If there is not any {@link RecommenderInfo} in the
	 *             collection, {@link #evaluate()} should be called first
	 */
	private RecommenderInfo getBestInfo() throws IllegalStateException {
		RecommenderInfo bestInfo;
		if (recommenders.size() > 0) {
			bestInfo = recommenders.iterator().next();
		} else {
			throw new IllegalStateException(
					"Evaluation hasn't been run yet. Run the evaluation first");
		}
		return bestInfo;
	}

	/**
	 * Deletes invalid {@link RecommenderInfo}s
	 */
	private void deleteNaNs() {
		Collection<RecommenderInfo> valids = new HashSet<RecommenderInfo>();
		for (RecommenderInfo recommender : recommenders) {
			if (!Double.isNaN(recommender.getScore())
					&& !Double.isInfinite(recommender.getScore())
					&& recommender.getScore() > 0) {
				valids.add(recommender);
			}
		}
		recommenders.clear();
		recommenders.addAll(valids);
	}

	/**
	 * Gets all the classes implementing {@link IRecommenderEval} compiled in
	 * the package </tt>org.weso.amg.evaluator</tt>
	 * 
	 * @return A collection of instances of all the available
	 *         {@link IRecommenderEval}s
	 */
	private Collection<IRecommenderEval> getAvailableEvaluators() {
		Collection<IRecommenderEval> evaluators = new HashSet<IRecommenderEval>();
		String pack = Evaluator.class.getPackage().toString().split(" ")[1];
		String[] beans = getEvaluatorsNames(pack);
		for (String bean : beans) {
			if (!bean.contains(".")) { // Delete spring classes
				if(bean.toLowerCase().contains("treec"))
				addEvaluator(evaluators, getFullyQualifiedName(pack, bean));
			}
		}
		return evaluators;
	}

	/**
	 * Adds an {@link IRecommenderEval} to the collection of
	 * {@link IRecommenderEval}s given
	 * 
	 * @param evaluators
	 *            The collection of the {@link IRecommenderEval}s where it will
	 *            be added
	 * @param bean
	 *            The fully qualified name of the new {@link IRecommenderEval}
	 */
	private void addEvaluator(Collection<IRecommenderEval> evaluators,
			String bean) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends IRecommenderEval> clazz = (Class<? extends IRecommenderEval>) Class
					.forName(bean);
			if (isFinal(clazz)) {
				evaluators.add(buildEvaluator(clazz));
			}
		} catch (ReflectiveOperationException e) {
			log.error("Error building an instance of " + bean
					+ ". This evaluation will not be run.", e);
		}
	}

	/**
	 * Gets the name of all the {@link IRecommenderEval}s contained in a package
	 * 
	 * @param packageName
	 *            The name of the package
	 * @return The name of all the {@link IRecommenderEval}s contained in a
	 *         package
	 */
	private String[] getEvaluatorsNames(String packageName) {
		BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(
				bdr);
		TypeFilter tf = new AssignableTypeFilter(IRecommenderEval.class);
		s.addIncludeFilter(tf);
		s.scan(packageName);
		String[] beans = bdr.getBeanDefinitionNames();
		return beans;
	}

	/**
	 * Builds an {@link IRecommenderEval} of a given class
	 * 
	 * @param clazz
	 *            The implementing class of the {@link IRecommenderEval}
	 * @return An instance implementing {@link IRecommenderEval}
	 * @throws InstantiationException
	 *             If the class represents an abstract class, an interface, an
	 *             array class, a primitive type, or void; or if the class has
	 *             no nullary constructor; or if the instantiation fails for
	 *             some other reason
	 * @throws IllegalAccessException
	 *             If the class or its nullary constructor is not accessible
	 */
	private IRecommenderEval buildEvaluator(
			Class<? extends IRecommenderEval> clazz)
			throws InstantiationException, IllegalAccessException {
		IRecommenderEval evaluator = clazz.newInstance();
		evaluator.setDataModel(model);
		evaluator.setEvaluator(this.evaluator);
		return evaluator;
	}

	/**
	 * Gets the fully qualified name of a class contained in a package
	 * 
	 * @param packageName
	 *            The name of the package where the class is contained
	 * @param bean
	 *            The name of the class
	 * @return The fully qualified name of the class
	 */
	private String getFullyQualifiedName(String packageName, String bean) {
		return new StringBuilder(packageName).append(".")
				.append(capitalize(bean)).toString();
	}

	/**
	 * Checks whether a {@link Class} is final or not
	 * 
	 * @param clazz
	 *            The {@link Class} to be checked
	 * @return <tt>false</tt> if the class is an interface or an abstract class,
	 *         <tt>true</tt> otherwise
	 */
	private boolean isFinal(Class<?> clazz) {
		return !clazz.isInterface()
				&& !Modifier.isAbstract(clazz.getModifiers());
	}

	/**
	 * Converts the first character of a string to upper case
	 * 
	 * @param bean
	 *            The string to be converted
	 * @return The string with the first character in upper case
	 */
	private String capitalize(String bean) {
		return bean.replaceFirst("" + bean.charAt(0),
				"" + Character.toUpperCase(bean.charAt(0)));
	}
}
