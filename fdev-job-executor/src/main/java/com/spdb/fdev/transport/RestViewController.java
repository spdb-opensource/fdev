//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spdb.fdev.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class RestViewController {
    @Autowired
    RestTransport restTransport;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RestViewController() {
    }

    @GetMapping({"/fjob/urlmapping"})
    public Map urlMapping() {
        return this.restTransport.getUrlMapping();
    }

    @GetMapping({"/fjob/urls"})
    public List invokeUrls() {
        Properties urlProperties = this.restTransport.getUrlMapping();
        ArrayList urlList = new ArrayList();
        Enumeration enumeration = urlProperties.keys();

        while(enumeration.hasMoreElements()) {
            String transCode = (String)enumeration.nextElement();
            urlList.add(urlProperties.get(transCode));
        }

        return urlList;
    }
}
