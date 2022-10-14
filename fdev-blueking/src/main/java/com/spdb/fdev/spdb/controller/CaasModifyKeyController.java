package com.spdb.fdev.spdb.controller;

import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.spdb.service.ICaasModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/3-15:55
 * @Description:caas_modifykey 控制层
 */
@RestController
@RequestMapping("/api/v2/caas")
public class CaasModifyKeyController {

    @Autowired
    private ICaasModifyKeyService caasModifyKeyService;

    /*
    * @author:guanz2
    * @Description:向caa_modifyKey表写入应用更新内容，为生成新的caas yaml文件提供数据
    */
    @PostMapping("/updateCaasModifyKey")
    @NoNull(parameter = ArrayList.class)
    public JsonResult updateCaasModifyKey(@RequestBody List<Map<String,String>> list) throws Exception {
        caasModifyKeyService.updateModifyKeys(list);
        return JsonResultUtil.buildSuccess();
    }

    //输入样例
//[
//    {
//        "deployment":"test",
//            "cluster":"shk1",
//            "namespace":"ebank-tenant",
//            "hostaliases":"DELETE",
//            "dnsconfig":"['xxx', 'xxx']"
//    },
//    {
//        "deployment":"test",
//            "cluster":"shk2",
//            "namespace":"ebank-tenant",
//            "hostaliases":"DELETE",
//            "dnsconfig":"['xxx', 'xxx']"
//    }
//]
}
