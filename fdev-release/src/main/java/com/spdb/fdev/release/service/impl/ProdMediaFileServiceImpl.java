package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.IProdMediaFileDao;
import com.spdb.fdev.release.entity.ProdMediaFile;
import com.spdb.fdev.release.service.IProdMediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdMediaFileServiceImpl implements IProdMediaFileService {

    @Autowired
    private IProdMediaFileDao prodMediaFileDao;

    @Override
    public void save(ProdMediaFile prodMediaFile) {
        prodMediaFileDao.save(prodMediaFile);
    }

    @Override
    public List<ProdMediaFile> findByProdId(String prod_id) {
        return prodMediaFileDao.findByProdId(prod_id);
    }

    @Override
    public void deleteByProdId(String prod_id) {
        prodMediaFileDao.deleteByProdId(prod_id);
    }
}
