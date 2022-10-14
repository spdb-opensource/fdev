package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdMediaFile;

import java.util.List;

public interface IProdMediaFileService {

    void save(ProdMediaFile prodMediaFile);

    List<ProdMediaFile> findByProdId(String prod_id);

    void deleteByProdId(String prod_id);
}
