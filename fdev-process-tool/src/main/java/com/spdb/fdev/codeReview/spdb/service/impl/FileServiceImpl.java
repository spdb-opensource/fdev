package com.spdb.fdev.codeReview.spdb.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.dict.ErrorConstants;
import com.spdb.fdev.codeReview.base.dict.OrderEnum;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.FileUtil;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.base.utils.UserUtil;
import com.spdb.fdev.codeReview.spdb.dao.IDictDao;
import com.spdb.fdev.codeReview.spdb.dao.IFileDao;
import com.spdb.fdev.codeReview.spdb.dao.IOrderDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeFile;
import com.spdb.fdev.codeReview.spdb.entity.CodeOrder;
import com.spdb.fdev.codeReview.spdb.service.IFileService;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.transport.RestTransport;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author liux81
 * @DATE 2021/11/12
 */
@Service
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Value("${fprocesstool.file.folder}")
    private String fileFolder;
    @Value("${file.download.temp.path}")
    private String fileTempPath;//文件中转路径

    @Autowired
    IDictDao dictDao;

    @Autowired
    IFileDao fileDao;

    @Autowired
    FileUtil fileUtil;

    @Autowired
    IOrderDao orderDao;

    @Autowired
    UserUtil userUtil;

    @Autowired
    private RestTransport restTransport;

    //@Async
    @Override
    public void uploadFile(String orderId, Integer orderStatus, String fileType, MultipartFile[] files, User user) throws Exception {
        if(CommonUtils.isNullOrEmpty(orderStatus)){
            CodeOrder codeOrder = orderDao.queryOrderById(orderId);
            orderStatus = codeOrder.getOrderStatus();
        }
        //终态下不可上传文件
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= orderStatus){
            throw new FdevException(ErrorConstants.DOC_ERROR_UPLOAD,new String[]{"工单终态下不可上传文件"});
        }
        for (MultipartFile multipartFile : files) {
            if (multipartFile.getSize() >= (50*1024*1024)) {
                throw new FdevException(ErrorConstants.FILE_INFO_SIZE_FILE + "！最大限制50M，文件"+multipartFile.getOriginalFilename() + multipartFile.getSize()/(1024*1024) + "M");
            }
        }
        List<String> listPathAll;
        //path:环境/工单id/文档类型/文件名
        String pathCommon = fileFolder +"/"+ orderId +"/"+ dictDao.queryDictByKey(fileType).getValue() +"/";
        //调用docmanage上传文件
        listPathAll = fileUtil.uploadFilestoMinio("fdev-process-tool", pathCommon, files, user);
        if(CommonUtils.isNullOrEmpty(listPathAll)) {
            throw new FdevException(ErrorConstants.DOC_ERROR_UPLOAD, new String[]{"上传文件失败,请重试!"});
        }
        //数据入库
        addFile(orderId,fileType,listPathAll,user);
    }

    @Override
    public void delete(String id) throws Exception {
        CodeFile queryCodeFile =  new CodeFile();
        queryCodeFile.setId(id);
        List<CodeFile> codeFile = fileDao.query(queryCodeFile);
        CodeOrder codeOrder = orderDao.queryOrderById(codeFile.get(0).getOrderId());
        //终态下不可删除文件
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            throw new FdevException(ErrorConstants.CAN_NOT_DEL_FILE, new String[]{"终态下不可删除文件"});
        }
        //删除minio文件
        User user = CommonUtils.getSessionUser();
        fileUtil.deleteFiletoMinio("fdev-process-tool", codeFile.get(0).getFilePath(),user);
        //删除数据库
        fileDao.delete(id);
    }

    @Override
    public Map<String,Object> queryFiles(String orderId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        CodeOrder codeOrder = orderDao.queryOrderById(orderId);
        String buttonFlag = "0";
        //添加上传按钮标识，终态下不可上传,删除
        if(OrderEnum.OrderStatusEnum.FIRST_PASS.getValue() <= codeOrder.getOrderStatus()){
            buttonFlag = "1";
        }
        List<CodeFile> codeFiles = fileDao.queryFilesByOrderId(orderId);
        resultMap.put(Dict.ADDBUTTON,buttonFlag);
        Set<String> userIds = new HashSet<>();
        for(CodeFile codeFile : codeFiles){
            userIds.add(codeFile.getUploadUser());
        }
        Map userInfo = userUtil.getUserByIds(userIds);
        for(CodeFile codeFile : codeFiles){
            codeFile.setDeleteButton(buttonFlag);
            codeFile.setFileTypeName(dictDao.queryDictByKey(codeFile.getFileType()).getValue());
            codeFile.setUploadUserName(userUtil.getUserNameById(codeFile.getUploadUser(),userInfo));
        }
        resultMap.put(Dict.FILELIST, codeFiles);
        return resultMap;
    }

    /**
     *下载工单下所有文件
     * 1、下载所有文件到挂载盘
     * 2、在挂载盘将文件打包压缩
     * 3、将压缩包以流的形式传给前端下载
     * 4、在挂载盘删除下载的文件和压缩包
     * @param orderId
     * @param response
     */
    @Override
    public void downloadAll(String orderId, HttpServletResponse response) throws Exception {
        //查询文件
        List<CodeFile> codeFiles = fileDao.queryFilesByOrderId(orderId);
        //在挂载盘新建一个压缩包，将文件一个一个的读入到压缩包中
        File zip = new File(fileTempPath + "/temp.zip");

        //循环下载
        for(CodeFile file : codeFiles) {
            String fileName = file.getFileName();
            String path = file.getFilePath();
            Map sendMap = new HashMap();
            if(path.contains("+")){
                path = URLEncoder.encode(path,"UTF-8");
            }
            sendMap.put(Dict.PATH, path);
            sendMap.put("moduleName", "fdev-process-tool");
            byte[] result = (byte[]) restTransport.submitGet("filesDownload", sendMap);
            byte[] buffer = new byte[3072];
            //下载任意文件
            if(!CommonUtils.isNullOrEmpty(result)) {
                //读取从minio下载的文件
                int count = -1;
                String newFileName = "【" + transFileType(file.getFileType()) + "】" + fileName;//避免文件名重复
                File file1 = new File(fileTempPath + newFileName);
                if (!file1.getParentFile().exists()) {
                    file1.getParentFile().mkdirs();
                }
                //将从minio读取的文件流写入挂载盘的文件中
                file1.createNewFile();
                file1.setWritable(true, false);
                file1.setReadable(true, false);
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zip));
                     InputStream in = new ByteArrayInputStream(result);
                     FileOutputStream fs = new FileOutputStream(file1);
                     FileInputStream fileInputStream = new FileInputStream(file1);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, 1024*3);){
                    while ((count = in.read(buffer)) != -1) {
                        fs.write(buffer,0,count);
                    }
                    fs.close();
                    //循环将文件写入压缩包
                    //读取下载的文件
                    byte[] buffer2 = new byte[1024*3];
                    zipOutputStream.putNextEntry(new ZipEntry(newFileName));
                    int l = 0;
                    //将文件写入压缩包
                    while ((l = bufferedInputStream.read(buffer2, 0, 1024*3)) != -1){
                        zipOutputStream.write(buffer2, 0, l);
                    }
                    fileInputStream.close();
                    bufferedInputStream.close();
                    //删除下载的文件和生成的临时压缩包
                    file1.delete();
                } catch (Exception e) {
                    //日志
                    logger.info("文件"+fileName+"打包异常:" + e.getMessage());
                }
            }

        }
        //将压缩包以文件流的形式读到response中，供前端下载
        CodeOrder order = orderDao.queryOrderById(orderId);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setHeader("Access-Control-Expose-Headers", "content-disposition");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String fileNameZip = new String((order.getOrderNo() + ".zip").getBytes(),"UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileNameZip);
        try(ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(zip);) {
            IOUtil.copyCompletely(inputStream, outputStream);
        } catch (Exception e) {
            logger.error("下载工单下所有文件错误", e);
        }
        zip.delete();
    }

    private String transFileType(String fileType) {
        return dictDao.queryDictByKey(fileType).getValue();
    }

    private void addFile(String orderId, String fileType, List<String> listPathAll, User user) throws JsonProcessingException {
        for (String path: listPathAll) {
            CodeFile codeFile = new CodeFile();
            codeFile.setOrderId(orderId);
            codeFile.setFilePath(path);
            codeFile.setFileType(fileType);
            String fileName = path.substring(path.lastIndexOf("/"));
            codeFile.setFileName(fileName.substring(1));
            List<CodeFile> codeFileList = fileDao.query(codeFile);
            if(CommonUtils.isNullOrEmpty(codeFileList)) {
                codeFile.setUploadUser(user.getId());
                codeFile.setUploadTime(TimeUtil.formatTodayHs());
                fileDao.save(codeFile);
            }else {
                CodeFile codeFileBefore = codeFileList.get(0);
                codeFile.setUploadUser(user.getId());
                codeFile.setUpdateTime(TimeUtil.formatTodayHs());
                codeFile.setId(codeFileBefore.getId());
                fileDao.updateById(codeFile);
            }
        }
    }
}
