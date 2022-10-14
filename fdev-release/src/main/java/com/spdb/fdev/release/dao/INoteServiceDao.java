package com.spdb.fdev.release.dao;

import java.util.List;

import com.spdb.fdev.release.entity.NoteService;

public interface INoteServiceDao {
	
	NoteService save(NoteService noteService);
    
	List<NoteService> queryNoteService(String noteId);
	
}
