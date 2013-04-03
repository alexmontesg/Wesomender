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

import es.weso.amg.recommender.model.Article;
import es.weso.amg.recommender.model.Media;
import es.weso.amg.recommender.model.Movie;
import es.weso.amg.recommender.model.Rating;

public class DatabaseBuilder {

	private String ratingsFileName, separator, newsFileName;

	private RatingDAO ratingDAO;
	private MovieDAO movieDAO;
	private ArticleDAO articleDAO;

	public DatabaseBuilder(String ratingsFileName, String newsFileName,
			String separator) {
		this.ratingsFileName = ratingsFileName;
		this.newsFileName = newsFileName;
		this.separator = separator;
		ratingDAO = new RatingDAO();
		movieDAO = new MovieDAO();
		articleDAO = new ArticleDAO();
	}

	public void buildDatabase() throws MongoException, NumberFormatException,
			IOException {
		buildRecommendationsTable();
		buildNewsTable();
	}

	protected void buildNewsTable() throws UnknownHostException,
			FileNotFoundException, IOException {
		articleDAO.deleteAll();
		File moviesFile = new File(newsFileName);
		FileInputStream fstream = new FileInputStream(moviesFile);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		Collection<Article> articles = new ArrayDeque<Article>();
		while ((strLine = br.readLine()) != null) {
			articles.add(lineToArticle(strLine));
		}
		articleDAO.add(articles);
		br.close();
		in.close();
		fstream.close();
	}

	protected void buildMoviesTable() throws UnknownHostException,
			FileNotFoundException, IOException {
		movieDAO.deleteAll();
		File moviesFile = new File(newsFileName);
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

	private Article lineToArticle(String strLine) {
		String[] data = strLine.split(separator);
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i].trim();
		}
		Article article = new Article();
		article.setItem_id(data[0]);
		article.setHeadline(data[1]);
		article.setText(data[2]);
		article.setLat(Double.parseDouble(data[3]));
		article.setLon(Double.parseDouble(data[4]));
		article.setTimestamp(Long.parseLong(data[5]));
		Media media = new Media();
		media.setName(data[6].split(",")[0]);
		media.setTrustworthiness(Double.parseDouble(data[6].split(",")[1]));
		article.setSource(media);
		article.setEntities(data[7].split(","));
		return article;
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
		if (timestamp < 0) {
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
