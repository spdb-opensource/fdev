package com.spdb.fdev.fdevapp.spdb.dao;

import java.util.List;

import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;

public interface IGitlabCIDao {
	public GitlabCI save(GitlabCI gitlabci) throws Exception;

	public List<GitlabCI> query(GitlabCI gitlabci) throws Exception;

	public GitlabCI update(GitlabCI gitlabci) throws Exception;

	public GitlabCI findById(String id) throws Exception;

    GitlabCI findByName(String name) throws Exception;
}
