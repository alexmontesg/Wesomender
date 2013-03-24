package es.weso.amg.recommender.model;

import java.util.Arrays;

public class Movie {
	private String title, item_id;
	private long created_at;
	private String[] genres;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public long getCreated_at() {
		return created_at;
	}

	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}

	public String[] getGenres() {
		return genres;
	}

	public void setGenres(String[] genres) {
		this.genres = genres;
	}
	
	public boolean isOfGenre(String genre) {
		for(String movieGenre : genres) {
			if(genre.equalsIgnoreCase(movieGenre)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Movie [title=" + title + ", item_id=" + item_id
				+ ", created_at=" + created_at + ", genres="
				+ Arrays.toString(genres) + "]";
	}
}
