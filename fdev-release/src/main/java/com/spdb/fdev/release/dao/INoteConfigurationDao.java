package com.spdb.fdev.release.dao;

import java.util.List;

import com.spdb.fdev.release.entity.NoteConfiguration;

public interface INoteConfigurationDao {
	
	NoteConfiguration save(NoteConfiguration noteConfig);
    
	List<NoteConfiguration> queryNoteConfiguration(String noteId);
	
	List<NoteConfiguration> queryByModule(String noteId,String module);
	
}
