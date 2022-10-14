package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.nonull.NoNull;
import com.spdb.fdev.fuser.spdb.entity.user.Section;
import com.spdb.fdev.fuser.spdb.service.ISectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author liux81
 * @DATE 2021/10/29
 */
@RequestMapping("/api/section")
@ResponseBody
@RestController
public class SectionController {

    @Autowired
    ISectionService sectionService;

    @PostMapping("/addSection")
    @NoNull(require = {"sectionNameEn","sectionNameCn"},parameter = Section.class)
    public JsonResult addSection(@RequestBody Section section) throws Exception {
        sectionService.addSection(section);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryAllSection")
    public JsonResult queryAllSection(){
        return JsonResultUtil.buildSuccess(sectionService.queryAllSection());
    }
}
