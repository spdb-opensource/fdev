package com.fdev.database.spdb.web;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.ErrorConstants;
import com.fdev.database.spdb.service.AppService;
import com.fdev.database.spdb.service.DatabaseService;
import com.fdev.database.spdb.service.FileService;
import com.fdev.database.util.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Api(tags = "文件操作接口")
@RequestMapping("/api/file")
@RestController
public class FileController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
    
    @Resource
    private FileService fileService;

    @Resource
    private DatabaseService databaseService;

    @Resource
    private AppService appService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /* Schema文件上传 */
    @ApiOperation(value = "Schema文件上传")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public JsonResult fileUpload(@RequestParam(Dict.DATABASE_TYPE) String database_type,
                                 @RequestParam(Dict.FILE) MultipartFile[] files) throws Exception {
        for(MultipartFile file : files){
           String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
           if(!fileType.equals("schema") && !fileType.equals("sql")){
               throw new FdevException(ErrorConstants.FILE_ERROR, new String[]{"上传文件类型错误"});
           }
        }
        JsonResult result = null;
        if (StringUtils.isBlank(database_type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        if (CommonUtils.isNullOrEmpty(files)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR);
        }
        //角色权限判断（应用负责人）
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if(!checkmManager(user)){
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"该用户权限不足"});
        }
        result = this.fileService.fileUpload(files, database_type, user);
        return result;
    }

    /**
     * @ 判断操作权限，用户为应用负责人、卡点管理员
     */
    private boolean checkmManager(User user) throws Exception {
        if( userVerifyUtil.userRoleIsSPM((List<String>) user.getRole_id())) {
            return true;
        }
        //是否为应用负责人
        List<Map> appList = appService.queryMyApps(user.getId());
        if(!CommonUtils.isNullOrEmpty(appList) && appList.size()>0)
            return true;
        return false;
    }


}
