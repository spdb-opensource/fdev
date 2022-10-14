package com.test.service;

import java.util.Map;

public interface BatchService {

	void batchConfigUser() throws Exception;

    void batchUserRoleLevelMantisToken() throws Exception;

    Map timingSyncFuser() throws Exception;
}
