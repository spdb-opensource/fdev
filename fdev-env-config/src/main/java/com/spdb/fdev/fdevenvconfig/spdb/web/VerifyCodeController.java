package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevenvconfig.spdb.service.IVerifyCodeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "验证码接口")
@RequestMapping("/api/v2/verifycode")
@RestController
public class VerifyCodeController {

    @Autowired
    private IVerifyCodeService iVerifyCodeService;

    /**
     * 生成验证码
     *
     * @param map
     * @return
     */
    @PostMapping("/getVerifyCode")
    public JsonResult getVerifyCode(@RequestBody Map map) throws Exception{
        return JsonResultUtil.buildSuccess(iVerifyCodeService.getVerifyCode(map));
    }
}
