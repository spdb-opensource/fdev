package com.spdb.fdev.pipeline.transport.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.pipeline.dao.IJobExeDao;
import com.spdb.fdev.pipeline.dao.IPipelineExeDao;
import com.spdb.fdev.pipeline.transport.GitlabTransport;
import com.spdb.fdev.pipeline.transport.RestTransService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Map;

@Service
public class RestTransServiceImpl implements RestTransService {

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private IPipelineExeDao pipelineExeDao;

    @Autowired
    private IJobExeDao jobExeDao;



    /**
     * 发送请求  根据请求类型区分发送哪个请求，并将结果获取
     *
     * @param sendData
     * @param requestUrl
     * @param requestType       请求类型
     * @throws Exception
     */
    @Override
    public Object restRequestByType(Map sendData, String requestUrl, String requestType) throws Exception {
        URI uri = UriComponentsBuilder.fromHttpUrl(requestUrl).build(true).toUri();
        if (Dict.POST.equals(requestType) || Dict.FORM_DATA.equals(requestType))
            return this.gitlabTransport.submitPost(uri, sendData);
        else if (Dict.GET.equals(requestType)) {
            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            return this.gitlabTransport.submitGet(uri.toString(), header);
        }else {
            return this.gitlabTransport.submitPost(uri, sendData);
        }
    }
}
