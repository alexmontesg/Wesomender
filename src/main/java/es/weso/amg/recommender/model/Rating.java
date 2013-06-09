package es.weso.amg.recommender.model;

public class Rating implements Comparable<Rating> {

	@Override
	public String toString() {
		return "Rating [user_id=" + user_id + ", item_id=" + item_id
				+ ", score=" + score + ", created_at=" + created_at + "]";
	}

	private String user_id, item_id;
	private double score;
	private long created_at;
	
	public Rating(){}

	public Rating(String user_id, String item_id, double score, long created_at) {
		this.user_id = user_id;
		this.item_id = item_id;
		this.score = score;
		this.created_at = created_at;
	}

	public Rating(String user_id, String item_id) {
		this.user_id = user_id;
		this.item_id = item_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public String getItem_id() {
		return item_id;
	}

	public double getScore() {
		return score;
	}

	public long getCreated_at() {
		return created_at;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}

	public int compareTo(Rating other) {
		if (this.score < other.score) {
			return -1;
		} else if (this.score > other.score) {
			return 1;
		}
		return 0;
	}

}
