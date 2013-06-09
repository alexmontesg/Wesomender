package es.weso.amg.recommender.persistence;

import java.util.ArrayDeque;
import java.util.Collection;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

import com.model.Video;

import es.weso.amg.recommender.model.Article;
import es.weso.amg.recommender.model.Media;

public class ArticleDAO {

	private ConfigurableApplicationContext ctx;
	private MongoOperations op;

	public ArticleDAO() {
		ctx = new GenericXmlApplicationContext("mongo-config2.xml");
		op = (MongoOperations) ctx.getBean("mongoTemplate");
	}

	public Article getArticle(String item_id) {
		Video video = op.findById(item_id, Video.class, "clips");
		Article article = videoToArticle(video);
		return article;
	}

	private Article videoToArticle(Video video) {
		Article article = new Article();
		String[] tags = new String[video.getTags().get("en").size()];
		int i = 0;
		for (String tag : video.getTags().get("en")) {
			tags[i++] = tag;
		}
		article.setEntities(tags);
		article.setItem_id(video.getId());
		article.setLat(video.getLat());
		article.setLon(video.getLon());
		Media media = new Media();
		media.setName(video.getUploadedBy());
		media.setTrustworthiness(Math.random());
		article.setSource(media);
		article.setTimestamp(article.getTimestamp());
		return article;
	}

	public Collection<Article> getArticlesNotIn(Collection<String> item_ids) {
		Collection<Video> videos = op.findAll(Video.class, "clips");
		Collection<Article> articles = new ArrayDeque<Article>(videos.size() - item_ids.size());
		for(Video video : videos) {
			if(!item_ids.contains(video.getId())) {
				articles.add(videoToArticle(video));
			}
		}
		return articles;
	}

	@Override
	protected void finalize() throws Throwable {
		ctx.close();
		super.finalize();
	}
}
