package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ProdMediaFile;

import java.util.List;

public interface IProdMediaFileDao {

    void save(ProdMediaFile prodMediaFile);

    List<ProdMediaFile> findByProdId(String prod_id);

    void deleteByProdId(String prod_id);
}
