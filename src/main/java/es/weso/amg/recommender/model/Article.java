package es.weso.amg.recommender.model;

import java.util.Arrays;

public class Article {

	private Media source;
	private double lat, lon;
	private String[] entities;
	private String headline, text;
	private long timestamp;
	private String item_id;

	public Media getSource() {
		return source;
	}

	public void setSource(Media source) {
		this.source = source;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String[] getEntities() {
		return entities;
	}

	public void setEntities(String[] entities) {
		this.entities = entities;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "Article [source=" + source + ", lat=" + lat + ", lon=" + lon
				+ ", entities=" + Arrays.toString(entities) + ", headline="
				+ headline + ", text=" + text + ", timestamp=" + timestamp
				+ ", item_id=" + item_id + "]";
	}

	public boolean hasEntity(String entity) {
		for (String e : entities) {
			if (e.equalsIgnoreCase(entity)) {
				return true;
			}
		}
		return false;
	}
}
