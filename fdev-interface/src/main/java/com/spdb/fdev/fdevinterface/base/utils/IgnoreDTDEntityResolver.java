package com.spdb.fdev.fdevinterface.base.utils;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;

/**
 * 解析xml文件时忽略DTD文件
 */
public class IgnoreDTDEntityResolver implements EntityResolver {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream("".getBytes()));
    }
}
