package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.DbReview;

import java.util.List;

public interface IDbReviewDao {

	DbReview query(String task_id);

	void update(DbReview dbReview1);

	void infoUpsert(DbReview dbReview1);

	List<DbReview> queryDbPath(String release_node_name, List<String> project_ids, String reviewStatus);

	List<DbReview> queryPath(String releaseDate, String group_abbr, String reviewStatus);
}
