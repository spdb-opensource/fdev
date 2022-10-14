package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IDbReviewDao;
import com.spdb.fdev.release.entity.DbReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class IDbReviewDaoImpl implements IDbReviewDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public DbReview query(String task_id) {
		Query query=new Query(Criteria.where("task_id").is(task_id));
		return mongoTemplate.findOne(query, DbReview.class);
	}

	@Override
	public void update(DbReview dbReview1) {
		Query query=new Query(Criteria.where("task_id").is(dbReview1.getTask_id()));
		Update update = Update.update("minIo_path", dbReview1.getMinIo_path()).set("reviewStatus",dbReview1.getReviewStatus());
		mongoTemplate.findAndModify(query, update, DbReview.class);
	}

	@Override
	public void infoUpsert(DbReview dbReview1) {
		Query query=new Query(Criteria.where("task_id").is(dbReview1.getTask_id()));
		Update update = Update.update("minIo_path", dbReview1.getMinIo_path())
				.set("release_node_name", dbReview1.getRelease_node_name())
				.set("project_id", dbReview1.getProject_id())
				.set("zip_path", dbReview1.getZip_path())
				.set("reviewStatus",dbReview1.getReviewStatus())
				.set("group_abbr",dbReview1.getGroup_abbr())
				.set("release_date",dbReview1.getRelease_date());
		mongoTemplate.upsert(query, update, DbReview.class);
	}

	@Override
	public List<DbReview> queryDbPath(String release_node_name, List<String> project_ids, String reviewStatus) {
		Query query=new Query(Criteria.where("release_node_name").is(release_node_name).and("project_id").in(project_ids).and("reviewStatus").is(reviewStatus));
		return mongoTemplate.find(query, DbReview.class);
	}


	@Override
	public List<DbReview> queryPath(String releaseDate, String group_id, String reviewStatus) {
		Query query=new Query(Criteria.where("release_date").is(releaseDate));
		List<DbReview> list = mongoTemplate.find(query, DbReview.class);
		List<DbReview> result = new ArrayList<>();
		for (DbReview db: list ) {
			if(group_id.equals(db.getGroup_abbr())){
				result.add(db);
			}

		}
		list.removeAll(result);
		result.addAll(list);
		return result;
	}
}
