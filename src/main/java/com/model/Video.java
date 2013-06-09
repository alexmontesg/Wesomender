package com.model;

import java.util.Date;
import java.util.Map;
import java.util.Vector;

/**
 * A {@link Video} is the result of processing the upload of a {@link User} or
 * the raw upload
 * 
 * @author <a href="http://alejandro-montes.appspot.com">Alejandro Montes
 *         García</a>
 * @since 23/07/2012
 * @version 1.5
 */
public class Video {

	private String id, uploadedBy;
	private Map<String, String> headline, description;
	private Map<String, Vector<String>> tags;
	private Date uploadDate, date;
	private Double lat, lon;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getHeadline() {
		return headline;
	}

	public void setHeadline(Map<String, String> headline) {
		this.headline = headline;
	}

	public Map<String, Vector<String>> getTags() {
		return tags;
	}

	public void setTags(Map<String, Vector<String>> tags) {
		this.tags = tags;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public void setDescription(Map<String, String> description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

}