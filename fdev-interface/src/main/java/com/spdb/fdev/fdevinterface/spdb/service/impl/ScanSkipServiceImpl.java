package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.spdb.service.ScanSkipService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
public class ScanSkipServiceImpl implements ScanSkipService {

    @Value("${scan.skip.startwith}")
    String skipScanStartWith;

    @Override
    public boolean skipAllScanFlag(String serviceId) {
        String[] appNameStartWith = skipScanStartWith.split(";");
        for (String skipAppName : appNameStartWith) {
            if (serviceId.startsWith(skipAppName)) {
                return true;
            }
        }
        return false;
    }
}
