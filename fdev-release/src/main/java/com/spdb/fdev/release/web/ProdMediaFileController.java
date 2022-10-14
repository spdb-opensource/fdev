package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ProdMediaFile;
import com.spdb.fdev.release.service.IProdMediaFileService;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RequestMapping("/api/prodMediaFile")
@RestController
public class ProdMediaFileController {

    private static final Logger logger = LoggerFactory.getLogger(ProdMediaFileController.class);

    @Autowired
    private IProdMediaFileService prodMediaFileService;

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @PostMapping(value = "/findByProdId")
    public JsonResult findByProdId(@RequestBody @ApiParam Map<String, String> requestParam) {
        String prod_id = requestParam.get(Dict.PROD_ID);
        return JsonResultUtil.buildSuccess(prodMediaFileService.findByProdId(prod_id));
    }

    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.DIRECTORY_LIST})
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String prod_id = (String)requestParam.get(Dict.PROD_ID);
        List<String> directory_list = (List<String>) requestParam.get(Dict.DIRECTORY_LIST);
        // 删除已有目录
        prodMediaFileService.deleteByProdId(prod_id);
        // 最上层目录集合（不重复）
        Set<String> dir_set = new HashSet<>();
        for(String dir : directory_list) {
            // 筛选最上层目录
            if(dir.indexOf("/") != -1) {
                dir_set.add(dir.split("/")[1]);
            } else {
                ObjectId id = new ObjectId();
                prodMediaFileService.save(new ProdMediaFile(id, id.toString(), prod_id, dir, null));
            }

        }
        for (String dir_outer : dir_set) {
            // 根据最上层目录分类
            List<String> dir_list = new ArrayList<>();
            for(String dir : directory_list) {
                if(dir.indexOf("/") != -1 && dir_outer.equals(dir.split("/")[1])) {
                    // 将最外层目录去除，保留后面小目录
                    dir_list.add(dir.substring(dir.indexOf(dir_outer) + dir_outer.length()));
                }
            }
            ObjectId id = new ObjectId();
            prodMediaFileService.save(new ProdMediaFile(id, id.toString(), prod_id, dir_outer, dir_list));
        }

        return JsonResultUtil.buildSuccess();
    }

}
