package com.spdb.fdev.component.service;

import java.io.IOException;

public interface IPythonService {
    String queryDependencyTree(String groupId, String artifactId, String version) throws IOException, InterruptedException;
}
