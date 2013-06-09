package es.weso.amg.recommender.serializer;

import java.io.Serializable;

import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

public abstract class RecommenderSerializer implements RecommenderBuilder,
		Serializable {

	// TODO Log errors
	private static final long serialVersionUID = -5196573405655912447L;
	public String className;

	public String getClassName() {
		return className;
	}

}
