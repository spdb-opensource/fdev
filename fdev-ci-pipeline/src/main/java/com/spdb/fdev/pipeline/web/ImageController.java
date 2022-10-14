package com.spdb.fdev.pipeline.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.annotation.nonull.NoNull;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.pipeline.entity.Images;
import com.spdb.fdev.pipeline.service.IImageService;
import com.spdb.fdev.pipeline.service.impl.DigitalServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private IImageService imageService;
    @Autowired
    private DigitalServiceImpl digitalService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("queryToolImageList")
    public JsonResult queryImageList(@RequestBody JsonNode jsonNode) throws Exception {
        return JsonResultUtil.buildSuccess(imageService.queryAllImages(jsonNode));
    }

    @PostMapping("addImage")
    @NoNull(require = {Dict.NAME, Dict.PATH}, parameter = Images.class)
    public JsonResult addImage(@RequestBody Images image) throws Exception {
        return JsonResultUtil.buildSuccess(imageService.addImage(image));
    }

    @PostMapping("updateImage")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult updateImage(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(imageService.updateImage(requestParam));
    }

    @PostMapping("findImageById")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult findImageById(@RequestBody Map<String, String> requestParam) {
        String id = requestParam.get(Dict.ID);
        return JsonResultUtil.buildSuccess(imageService.findImageById(id));
    }
}
