package es.weso.amg.recommender.persistence;

import java.util.Collection;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import es.weso.amg.recommender.model.Movie;

public class MovieDAO {

	private ConfigurableApplicationContext ctx;
	private MongoOperations op;

	public MovieDAO() {
		ctx = new GenericXmlApplicationContext("mongo-config.xml");
		op = (MongoOperations) ctx.getBean("mongoTemplate");
	}

	public void deleteAll() {
		op.dropCollection(Movie.class);
	}

	public void add(Movie m) {
		op.insert(m);
	}

	public void add(Collection<Movie> m) {
		op.insertAll(m);
	}

	public Movie getMovie(String item_id) {
		return op.findOne(new Query(Criteria.where("item_id").is(item_id)),
				Movie.class);
	}

	public Collection<Movie> getMoviesNotIn(Collection<String> item_ids) {
		return op.find(new Query(Criteria.where("item_id").nin(item_ids)),
				Movie.class);
	}

	public Collection<Movie> getMovies(Collection<String> item_ids) {
		return op.find(new Query(Criteria.where("item_id").in(item_ids)),
				Movie.class);
	}

	public Collection<Movie> getAllMovies() {
		return op.findAll(Movie.class);
	}

	@Override
	protected void finalize() throws Throwable {
		ctx.close();
		super.finalize();
	}

}
