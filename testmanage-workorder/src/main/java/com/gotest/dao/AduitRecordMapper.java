package com.gotest.dao;

import com.gotest.domain.AduitRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AduitRecordMapper {


    Integer addRecordPassOrder(AduitRecord aduitRecord);

    AduitRecord queryByAduitWorkNo(@Param("aduitWorkNo")String aduitWorkNo);

    Integer updateAduitWorkNoByAduitId(@Param("aduitId")Integer aduitId,@Param("aduitWorkNo")String aduitWorkNo);

    Integer updateAduitWorkNoByWorkNos(@Param("aduitWorkNos")List<String> aduitWorkNos,@Param("aduitWorkNo")String aduitWorkNo);
}
