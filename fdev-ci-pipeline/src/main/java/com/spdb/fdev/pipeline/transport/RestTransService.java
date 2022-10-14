package com.spdb.fdev.pipeline.transport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface RestTransService {


    Object restRequestByType(Map sendData, String requestUrl, String requestType) throws Exception;
}
