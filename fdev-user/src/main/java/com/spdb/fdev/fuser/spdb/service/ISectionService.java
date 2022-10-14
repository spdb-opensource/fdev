package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.Section;
import java.util.List;

public interface ISectionService {
    void addSection(Section section) throws Exception;

    List<Section> queryAllSection();
}
