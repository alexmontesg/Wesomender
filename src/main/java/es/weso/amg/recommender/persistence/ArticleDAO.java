package es.weso.amg.recommender.persistence;

import java.util.Collection;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import es.weso.amg.recommender.model.Article;

public class ArticleDAO {

	private ConfigurableApplicationContext ctx;
	private MongoOperations op;

	public ArticleDAO() {
		ctx = new GenericXmlApplicationContext("mongo-config.xml");
		op = (MongoOperations) ctx.getBean("mongoTemplate");
	}
	
	public void deleteAll() {
		op.dropCollection(Article.class);
	}

	public void add(Article article) {
		op.insert(article);
	}

	public void add(Collection<Article> articles) {
		op.insertAll(articles);
	}

	public Article getArticle(String item_id) {
		return op.findOne(new Query(Criteria.where("item_id").is(item_id)),
				Article.class);
	}

	public Collection<Article> getArticlesNotIn(Collection<String> item_ids) {
		return op.find(new Query(Criteria.where("item_id").nin(item_ids)),
				Article.class);
	}

	public Collection<Article> getArticles(Collection<String> item_ids) {
		return op.find(new Query(Criteria.where("item_id").in(item_ids)),
				Article.class);
	}

	public Collection<Article> getAllArticles() {
		return op.findAll(Article.class);
	}
	
	@Override
	protected void finalize() throws Throwable {
		ctx.close();
		super.finalize();
	}
}
