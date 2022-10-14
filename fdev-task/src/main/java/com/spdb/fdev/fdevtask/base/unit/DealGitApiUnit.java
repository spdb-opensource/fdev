package com.spdb.fdev.fdevtask.base.unit;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 处理gitapi相关请求组件
 */
@Component
@RefreshScope
public class DealGitApiUnit {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${git.token}")
    private String token;

    @Value("${limitCommit.value}")
    private Integer limitCommitValue;//触发阈值
    @Value("${limitConflict.value}")
    private Integer limitConflict;//触发阈值

    public Map sendReqToGitServer(String url) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(headers);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        HashMap map = new HashMap();
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            map = JSON.parseObject(result.getBody(), HashMap.class);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info("调用gitlab服务败请求url:{}Error Trace:{}",
                    url,
                    sw.toString());
            return Collections.EMPTY_MAP;
        }
        return map;
    }

    /**
     * 获取文件清单和冲突数和落后commit数量
     *
     * @return
     */
    public Tuple.Tuple3 getListAndCommits(Map map) {
        // 计算落后版本数字 commits
        Integer commits = JSON.parseArray(map.get("commits").toString()).size();
        Integer size = commits == 0 ? 0 : commits - 1;
        // 计算冲突如文件和冲突个数 diffs
        Object diffs = map.get("diffs");
        AtomicInteger confiltNum = new AtomicInteger();
        List<Object> collect = JSON.parseArray(diffs.toString())
                .stream().filter(a -> {
                    Map diffsMap = JSON.parseObject(a.toString(), Map.class);
                    if (CommonUtils.isNullOrEmpty(diffsMap))
                        return false;
                    if ((Boolean) diffsMap.get("new_file"))
                        return false;
                    if (CommonUtils.isNullOrEmpty(diffsMap.get("diff")))
                        return false;
                    Integer diff = getDiffs(diffsMap.get("diff").toString());
                    if (diff > 0) {
                        confiltNum.addAndGet(diff);
                        return true;
                    }
                    return false;
                }).collect(Collectors.toList());

        //超过阈值并且没冲突直接返回
        if (limitCommitValue >= size && limitConflict >= confiltNum.get()) {
            logger.info("没有超过落后阈值:{}{}", size, limitCommitValue);
            logger.info("没有超过冲突阈值:{}冲突阈值配置{}", confiltNum.get(), limitConflict);
            return Tuple.tuple(0, 0, Collections.emptyList());
        }
        if (collect.size() == 0) {
            return Tuple.tuple(size, 0, Collections.emptyList());
        }
        //冲突文件列表
        List<String> new_path = new ArrayList<>();
        if (confiltNum.get() > 0) {
            new_path = collect.stream().flatMap(a -> {
                Map diffsMap = JSON.parseObject(a.toString(), Map.class);
                return Arrays.asList(diffsMap.get("new_path").toString()).stream();
            }).collect(Collectors.toList());
        }
        return Tuple.tuple(size, confiltNum.get(), new_path);
    }

    public Integer getDiffs(String str) {
        Pattern compile = Pattern.compile("@@(?<first>[\\s-+,0-9]{1,})@@");
        Matcher matcher = compile.matcher(str);
        ArrayList<String> list = new ArrayList<>();
        AtomicInteger num = new AtomicInteger();
        while (matcher.find()) {
            String first = matcher.group("first")
                    .replace(" ", "")
                    .replace(",", "")
                    .replace("-", "");
            list.add(first);
        }
        list.stream().forEach(diff -> {
            String[] split = diff.split("\\+");
            if (split.length > 1 && split[0].equals(split[1])) {
                num.getAndIncrement();
            }
        });
        return num.get();
    }

    /**
     * 自定义返回类型
     *
     * @param url   请求地址
     * @param clazz 类型
     * @param <R>   入参类型
     * @return 响应
     * @throws Exception 类型转换
     */
    public <R> R sendReqToGitServer(String url, Class<R> clazz) throws Exception {
        Object obj = Class.forName(clazz.getName()).newInstance();
        if (clazz != null && !clazz.isInstance(obj))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数类型不匹配"});
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(Dict.PRIVATE_TOKEN, token);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addAll(headers);
        HttpEntity<Object> request = new HttpEntity<Object>(httpHeaders);
        R resp = (R) obj;
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (clazz.newInstance() instanceof List) {
                resp = (R) JSON.parseArray(result.getBody());
            }
            if (clazz.newInstance() instanceof Map) {
                resp = JSON.parseObject(result.getBody(), clazz);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.info("调用gitlab服务败请求url:{}Error Trace:{}",
                    url,
                    sw.toString());
            return resp;
        }
        return resp;
    }
}