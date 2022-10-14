package com.spdb.fdev.fdemand.spdb.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import com.spdb.fdev.common.User;

public interface FdocmanageService {
	
	List<String> uploadFilestoMinio(String moduleName, String pathCommon, MultipartFile[] multipartFile, User user);
		
	boolean deleteFiletoMinio(String moduleName, String path, User user);

	boolean deleteFiletoMinio(String moduleName, List<String> paths, User user);

	Map createFolder(String id);

	Map<String, List> wpsList(String folderId);

	Map content(String id);

	boolean downloadAndUpload(String url, String pathMinio, String name);

	InputStreamResource downloadFileInputStream(String moduleName, String filePath);

	byte[] downloadFileByte(String moduleName, String filePath);
}
