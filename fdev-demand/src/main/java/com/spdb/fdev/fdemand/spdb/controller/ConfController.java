package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.ConfUtils;
import com.spdb.fdev.fdemand.spdb.dto.conf.ContentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/conf")
public class ConfController {

    @Autowired
    private ConfUtils confUtils;

    /**
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/addFileLabel")
    public JsonResult addFileLabel(@RequestBody String pageId) throws Exception {
        //就一次性接口而已
        //嘿，这次我就偏不写在service里面了，懒
        Set<String> totalPageIds = new HashSet<>();
        totalPageIds.add(pageId);
        addTotalPageIds(pageId, totalPageIds);
        //该方法踩了莫名的坑 暂时屏蔽
//        List<Map<String, String>> body = new ArrayList<Map<String, String>>() {{
//            add(new HashMap<String, String>() {{
//                put("prefix", "global");
//                put("name", "已归档");
//            }});
//        }};
        // totalPageIds.forEach(item -> confUtils.sendPost(confUtils.getConfApi() + "/content/" + item + "/label", body));
        totalPageIds.forEach(item -> confUtils.addLabel(item));
        return JsonResultUtil.buildSuccess();
    }

    private void addTotalPageIds(String pageId, Set<String> result) {
        List<ContentDto> totalPageData = confUtils.getTotalPageData("/content/" + pageId + "/child/page?");
        if (!CommonUtils.isNullOrEmpty(totalPageData)) {
            for (ContentDto item : totalPageData) {
                result.add(item.getId());
                addTotalPageIds(item.getId(), result);
            }
        }
    }
}
