package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ISccModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/6-13:16
 * @Description: scc_modifyKey 控制层
 */

@RestController
@RequestMapping("/api/v2/scc")
public class SccModifyKeyController {

    @Autowired
    private ISccModifyKeyService sccModifyKeyService;

    /*
    * @author:guanz2
    * @Description: 向scc_modifyKey表写入应用更新内容，为生成新的scc yaml文件提供数据
    */
    @PostMapping("/updateSccModifyKey")
    @NoNull(parameter = ArrayList.class)
    public JsonResult updateSccModifyKey(@RequestBody List<LinkedHashMap<String,Object>> list) throws Exception {
        sccModifyKeyService.updateModifyKeys(list);
        return JsonResultUtil.buildSuccess();
    }

    //输入样例
//   [
//      {
//            "namespace_code": "pxer",
//            "resource_code": "mspxer-web-pxer",
//            "yaml": "xxxxxxxxxxx"
//   ]
}


