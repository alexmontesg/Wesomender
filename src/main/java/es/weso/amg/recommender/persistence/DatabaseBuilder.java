package es.weso.amg.recommender.persistence;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.MongoException;

import es.weso.amg.recommender.model.Movie;
import es.weso.amg.recommender.model.Rating;

public class DatabaseBuilder {

	private String ratingsFileName, separator, moviesFileName;

	private RatingDAO ratingDAO;
	private MovieDAO movieDAO;

	public DatabaseBuilder(String ratingsFileName, String moviesFileName,
			String separator) {
		this.ratingsFileName = ratingsFileName;
		this.moviesFileName = moviesFileName;
		this.separator = separator;
		ratingDAO = new RatingDAO();
		movieDAO = new MovieDAO();
	}

	public void buildDatabase() throws MongoException, NumberFormatException,
			IOException {
		buildRecommendationsTable();
		buildMoviesTable();
	}

	protected void buildMoviesTable() throws UnknownHostException,
			FileNotFoundException, IOException {
		movieDAO.deleteAll();
		File moviesFile = new File(moviesFileName);
		FileInputStream fstream = new FileInputStream(moviesFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		Collection<Movie> movies = new ArrayDeque<Movie>();
		while ((strLine = br.readLine()) != null) {
			movies.add(lineToMovie(strLine));
		}
		movieDAO.add(movies);
		br.close();
		in.close();
		fstream.close();
	}

	protected void buildRecommendationsTable() throws UnknownHostException,
			FileNotFoundException, IOException {
		ratingDAO.deleteAll();
		File ratingsFile = new File(ratingsFileName);
		FileInputStream fstream = new FileInputStream(ratingsFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		Collection<Rating> ratings = new ArrayDeque<Rating>();
		while ((strLine = br.readLine()) != null) {
			ratings.add(lineToRecommendation(strLine));
		}
		ratingDAO.add(ratings);
		br.close();
		in.close();
		fstream.close();
	}

	private Movie lineToMovie(String strLine) {
		String[] data = strLine.split(separator);
		Movie movie = new Movie();
		movie.setItem_id(data[0]);
		Pattern r = Pattern.compile("[(](1|2)\\d{3}[)]$");
		Matcher m = r.matcher(data[1]);
		m.find();
		String yearWithParenthesis = m.group(0);
		movie.setTitle(data[1].replace(yearWithParenthesis, "").trim());
		r = Pattern.compile("\\d{4}");
		m = r.matcher(yearWithParenthesis);
		m.find();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, Integer.parseInt(m.group(0)));
		long timestamp = c.getTimeInMillis(); 
		if(timestamp < 0) {
			timestamp = 0;
		}
		movie.setCreated_at(timestamp);
		movie.setGenres(data[2].split("\\|"));
		return movie;
	}

	private Rating lineToRecommendation(String strLine) {
		String[] data = strLine.split(separator);
		Rating rating = new Rating(data[0], data[1]);
		if (data.length > 2) {
			rating.setScore(Double.parseDouble(data[2]));
			if (data.length > 3) {
				rating.setCreated_at(Long.parseLong(data[3]));
			}
		}
		return rating;
	}
}
