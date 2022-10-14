package com.spdb.fdev.fdemand.spdb.service;

import java.io.FileNotFoundException;
import java.util.List;

public interface IGitlabService {
    void uploadFile(List<byte[]> files, String path);
}
