package es.weso.amg.recommender.model;

import java.io.Serializable;

import es.weso.amg.recommender.serializer.AbstractSerializer;

/**
 * Stores pairs of {@link AbstractSerializer}s and their scores after an
 * evaluation, instances of this class can be compared (the lower the score, the
 * higher the instance). Instances of this class can also be serialized
 * 
 * @author Alejandro Montes García <alejandro.montes@weso.es>
 * @since 21/12/2012
 * 
 * @see Serializable
 * @see Comparable
 * 
 */
public class RecommenderInfo implements Comparable<RecommenderInfo>,
		Serializable {

	private static final long serialVersionUID = -3093652567909102382L;
	private AbstractSerializer abstractSerializer;
	private double score;

	/**
	 * Stores pairs of {@link AbstractSerializer}s and their scores after an
	 * evaluation
	 * 
	 * @param abstractSerializer
	 *            The {@link AbstractSerializer} evaluated
	 * @param score
	 *            The score of the evaluation
	 */
	public RecommenderInfo(AbstractSerializer abstractSerializer, double score) {
		this.abstractSerializer = abstractSerializer;
		this.score = score;
	}

	/**
	 * Gets the {@link AbstractSerializer} that was evaluated
	 * 
	 * @return The {@link AbstractSerializer} that was evaluated
	 */
	public AbstractSerializer getAbstractSerializer() {
		return abstractSerializer;
	}

	/**
	 * Gets the score associated with the {@link #getAbstractSerializer()
	 * recommenderSerializer}
	 * 
	 * @return The score associated with the {@link #getAbstractSerializer()
	 *         serializer}
	 */
	public double getScore() {
		return score;
	}

	public int compareTo(RecommenderInfo obj) {
		if (this.score - obj.score < 0.0) {
			return -1;
		} else if (this.score == obj.score) {
			return 0;
		} else
			return 1;
	}

}
