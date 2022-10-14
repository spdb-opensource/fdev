package com.spdb.fdev.component.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.service.IComponentInfoService;
import com.spdb.fdev.component.service.IMailService;
import com.spdb.fdev.component.service.IMpassComponentService;
import com.spdb.fdev.component.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RefreshScope
@RequestMapping("/api/mail")
public class MailController {

    @Value("${history.component.url}")
    private String history_component_url;

    @Value("${history.mpasscomponent.url}")
    private String history_mpasscomponent_url;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IMpassComponentService mpassComponentService;

    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.JDK_VERSION,
            Dict.TARGET_VERSION, Dict.RELEASE_LOG, Dict.UPDATE_USER})
    @PostMapping("/mailContent")
    public JsonResult getMailContent(@RequestBody Map map) throws Exception {
        HashMap hashMap = new HashMap();
        //组件ID 组件版本 组件历史记录 组件历史链接 使用组件的人
        String component_id = (String) map.get(Dict.COMPONENT_ID);
        String jdk = (String) map.get(Dict.JDK_VERSION);
        String target_version = (String) map.get(Dict.TARGET_VERSION);
        String version = getVersion(jdk, target_version);
        String release_log = (String) map.get(Dict.RELEASE_LOG);
        String update_user = (String) map.get(Dict.UPDATE_USER);
        //查询用户信息
        Map<String, Object> userMap = roleService.queryUserbyid(update_user);
        //1.通过组件id查询组件名称
        ComponentInfo componentInfo = componentInfoService.queryById(component_id);
        String component_name_cn = componentInfo.getName_cn();
        hashMap.put(Dict.NAME_CN, component_name_cn);
        //2.组件历史链接
        String history = history_component_url + component_id;
        hashMap.put(Dict.VERSION, version);
        hashMap.put(Dict.RELEASE_LOG, release_log);
        hashMap.put(Dict.HISTORY_COMPONENT_URL, history);
        hashMap.put(Dict.USER_NAME_CN, userMap.get(Dict.USER_NAME_CN));
        //3.获取邮件内容
        String mailContent = mailService.getMailContent(hashMap);
        Map param = new HashMap();
        param.put(Dict.EMAIL_CONTENT, mailContent);
        return JsonResultUtil.buildSuccess(param);
    }


    /**
     * 获取mpass组件废弃邮件内容
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.COMPONENT_ID, Dict.VERSION,
            Dict.RELEASE_LOG, Dict.UPDATE_USER})
    @PostMapping("/mpassMailDestroyContent")
    public JsonResult mpassMailDestroyContent(@RequestBody Map map) throws Exception {
        HashMap hashMap = new HashMap();
        //组件ID 组件版本 组件历史记录 组件历史链接 使用组件的人
        String component_id = (String) map.get(Dict.COMPONENT_ID);
        String version = (String) map.get(Dict.VERSION);
        String release_log = (String) map.get(Dict.RELEASE_LOG);
        String update_user = (String) map.get(Dict.UPDATE_USER);
        //查询用户信息
        Map<String, Object> userMap = roleService.queryUserbyid(update_user);
        //1.通过组件id查询组件名称
        MpassComponent mpassComponent = mpassComponentService.queryById(component_id);
        String component_name_cn = mpassComponent.getName_cn();
        hashMap.put(Dict.NAME_CN, component_name_cn);
        //2.组件历史链接
        String history = history_mpasscomponent_url + component_id;
        hashMap.put(Dict.VERSION, version);
        hashMap.put(Dict.RELEASE_LOG, release_log);
        hashMap.put(Dict.HISTORY_MPASSCOMPONENT_URL, history);
        hashMap.put(Dict.USER_NAME_CN, userMap.get(Dict.USER_NAME_CN));
        //3.获取邮件内容
        String mailContent = mailService.mpassMailDestroyContent(hashMap);
        Map param = new HashMap();
        param.put(Dict.EMAIL_CONTENT, mailContent);
        return JsonResultUtil.buildSuccess(param);
    }

    /**
     * 根据jdk版本
     *
     * @param jdk
     * @param target_version
     * @return
     */
    public String getVersion(String jdk, String target_version) {
        if ("1.7".equals(jdk)) {
            return target_version + "-jdk7" + Dict._RELEASE;
        }
        return target_version + Dict._RELEASE;
    }


}
