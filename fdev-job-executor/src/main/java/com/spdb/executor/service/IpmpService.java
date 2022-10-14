package com.spdb.executor.service;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public interface IpmpService {

    List<Map<String, String>> queryIpmpSystemInfo();
}
