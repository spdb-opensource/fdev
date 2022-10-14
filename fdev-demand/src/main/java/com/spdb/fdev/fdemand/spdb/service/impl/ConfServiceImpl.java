package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.ConfUtils;
import com.spdb.fdev.fdemand.spdb.dto.conf.ContentDto;
import com.spdb.fdev.fdemand.spdb.service.IConfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConfServiceImpl implements IConfService {

    @Autowired
    private ConfUtils confUtils;

    @Value("${confluence.template.id:a49d7fe2-5b8b-4817-9f4f-9127dbbf2152}")
    private String templateId;

    @Override
    public void addFileLabel(String pageId) {
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
    }

    @Override
    public List<ContentDto> getConfluencePageInfo(String pageId) {
        List<ContentDto> totalPageData = confUtils.getTotalPageData("/content/" + pageId + "/child/page?");
        if (!CommonUtils.isNullOrEmpty(totalPageData)) {
            for (ContentDto item : totalPageData) {
                getConfluencePageInfo(item.getId());
            }
        }
        return totalPageData;
    }

    @Override
    public byte[] exportWord(String pageId) throws Exception {
        return confUtils.exportWord(pageId, templateId);
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
