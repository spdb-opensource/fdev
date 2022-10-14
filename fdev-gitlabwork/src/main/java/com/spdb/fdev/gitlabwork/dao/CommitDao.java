package com.spdb.fdev.gitlabwork.dao;

import com.spdb.fdev.gitlabwork.entiy.Commit;

import java.util.Collection;
import java.util.List;

public interface CommitDao {

    List<Commit> findUnStats();

    List<Commit> findAll();

    void updateStats(Commit commit);

    void upert(Commit commit);

    void upert(Collection<Commit> record);

    void upertCommittedTitle(Collection<Commit> record);
}
