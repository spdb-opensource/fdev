package com.fdev.database.spdb.service;


import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {


    JsonResult fileUpload(MultipartFile[] files, String database_type, User user) throws Exception;
}
