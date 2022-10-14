//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spdb.fdev.common;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class FdevResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Value("${no.filter.response.urls:}")
    private String nofilterUrl = "";

    public FdevResponseBodyAdvice() {
    }

    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpRequest req = (ServletServerHttpRequest)request;
        String requestURI = req.getServletRequest().getRequestURI();
        String[] urls = this.nofilterUrl.split(";");
        HashSet<String> urlSet = new HashSet(Arrays.asList(urls));
        if (!urlSet.contains(requestURI)) {
            try {
                if (body instanceof JsonResult) {
                    JSONObject result = new JSONObject();
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setSerializationInclusion(Include.ALWAYS);
                    String json = null;

                    try {
                        json = body == null ? "{}" : mapper.writeValueAsString(body);
                    } catch (JsonProcessingException var15) {
                        return body;
                    }

                    if (null != json) {
                        JSONObject pJson = JSONObject.fromObject(json);
                        result = this.filter(pJson);
                    }

                    return result;
                } else {
                    return body;
                }
            } catch (Exception var16) {
                return body;
            }
        } else {
            return body;
        }
    }

    public JSONObject filter(JSONObject datas) {
        JSONObject temp = new JSONObject();
        Iterator da = datas.keys();

        while(true) {
            while(true) {
                String ki;
                Object vi;
                do {
                    do {
                        if (!da.hasNext()) {
                            return temp;
                        }

                        ki = (String)da.next();
                        vi = datas.get(ki);
                    } while("_id".equals(ki));
                } while("password".equals(ki));

                if (null != vi && !(vi instanceof JSONNull)) {
                    if (vi instanceof String) {
                        try {
                            JSONObject str = JSONObject.fromObject(vi);
                            temp.put(ki, this.filter(str));
                        } catch (Exception var7) {
                            temp.put(ki, vi);
                        }
                    } else if (vi instanceof JSONArray) {
                        temp.put(ki, this.filter((JSONArray)vi));
                    } else if (vi instanceof JSONObject) {
                        temp.put(ki, this.filter((JSONObject)vi));
                    } else {
                        temp.put(ki, vi);
                    }
                } else if ("count".equals(ki)) {
                    temp.put(ki, 0);
                } else {
                    temp.put(ki, "");
                }
            }
        }
    }

    public JSONArray filter(JSONArray datas) {
        JSONArray result = new JSONArray();
        Iterator var3 = datas.iterator();

        while(var3.hasNext()) {
            Object da = var3.next();
            if (null != da) {
                if (da instanceof String) {
                    result.add(da);
                }

                if (da instanceof JSONArray) {
                    result.add(this.filter((JSONArray)da));
                }

                if (da instanceof JSONObject) {
                    result.add(this.filter((JSONObject)da));
                }
            }
        }

        return result;
    }
}
