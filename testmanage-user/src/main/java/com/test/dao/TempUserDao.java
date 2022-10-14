package com.test.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface TempUserDao {
    void batchInsertTempUser(@Param("userNameEns")List<String> userNameEns) throws Exception;

    void deleteTempUser() throws Exception;
}
