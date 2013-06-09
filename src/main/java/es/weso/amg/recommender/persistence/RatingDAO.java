package es.weso.amg.recommender.persistence;

import java.util.Collection;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import es.weso.amg.recommender.model.Rating;

public class RatingDAO {

	private ConfigurableApplicationContext ctx;
	private MongoOperations op;

	public RatingDAO() {
		ctx = new GenericXmlApplicationContext("mongo-config.xml");
		op = (MongoOperations) ctx.getBean("mongoTemplate");
	}

	public void deleteAll() {
		op.dropCollection(Rating.class);
	}

	public void add(Rating r) {
		op.insert(r);
	}

	public void add(Collection<Rating> r) {
		op.insertAll(r);
	}

	public Rating getRating(String user_id, String item_id) {
		return op.findOne(
				new Query(Criteria.where("user_id").is(user_id).and("item_id")
						.is(item_id)), Rating.class);
	}

	public Collection<Rating> getRatingsFromUser(String user_id) {
		return op.find(new Query(Criteria.where("user_id").is(user_id)),
				Rating.class);
	}

	@Override
	protected void finalize() throws Throwable {
		ctx.close();
		super.finalize();
	}

}
