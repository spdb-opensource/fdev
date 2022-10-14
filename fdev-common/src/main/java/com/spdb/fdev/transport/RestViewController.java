package com.spdb.fdev.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by xxx on 上午10:24.
 */
@RestController
public class RestViewController {

    @Autowired RestTransport restTransport;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/rest/urlmapping")
    public Map urlMapping(){
        return restTransport.getUrlMapping();
    }

    @GetMapping("/rest/urls")
    public List invokeUrls(){
        Properties urlProperties = restTransport.getUrlMapping();
        ArrayList urlList = new ArrayList();
        for(Enumeration enumeration = urlProperties.keys();enumeration.hasMoreElements();){
            String transCode = (String) enumeration.nextElement();
            urlList.add(urlProperties.get(transCode));
        }
        return urlList;
    }
}
