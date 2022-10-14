package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.StringWriter;
import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/12
 */
@Component
public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    @Autowired
    RestTransport restTransport;

    public List<String> uploadFilestoMinio(String moduleName, String pathCommon, MultipartFile[] multipartFile, User user) {
        List<String> listPathAlList = new ArrayList<String>();
        try {
            for (MultipartFile file : multipartFile) {
                String fileNameString = file.getOriginalFilename();
                String pathAll = pathCommon  + fileNameString;
                Map<String, Object> param = new HashMap<>();
                param.put(Dict.MODULENAME, moduleName);
                param.put(Dict.PATH , pathAll);
                param.put(Dict.USER, user);
                param.put(Dict.FILES, file.getResource());
                restTransport.submitUpload(Dict.FILESUPLOAD, param);
                listPathAlList.add(pathAll);
            }
        } catch (Exception e) {
            throw new FdevException("文档上传失败");
        }

        return listPathAlList;
    }

    /**
     * 删除文档文件
     *
     * @param moduleName 实体
     * @param path       路径
     * @return
     */
    public boolean deleteFiletoMinio(String moduleName, String path, User user) {

        try {
            restTransport.submit(new HashMap() {{
                put(Dict.MODULENAME, moduleName);
                put(Dict.PATH, Arrays.asList(path));
                put(Dict.USER, user);
                put(Dict.REST_CODE, Dict.FILESDELETE);
            }});
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            log.info("调用文档模块失败！请求参数:{},{}Error Trace:{}",
                    moduleName,
                    path,
                    sw.toString());
            return false;
        }
        return true;
    }
}
