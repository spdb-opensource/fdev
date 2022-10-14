package com.spdb.fdev.release.dao;

import java.util.List;

import com.spdb.fdev.release.entity.NoteSql;

public interface INoteSqlDao {
	
	NoteSql save(NoteSql noteSql);
    
	List<NoteSql> queryNoteSql(String noteId);
	
	
	
}
