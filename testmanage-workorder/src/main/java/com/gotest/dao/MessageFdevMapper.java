package com.gotest.dao;

import com.gotest.domain.MessageFdev;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Repository
public interface MessageFdevMapper {


    List<MessageFdev> queryMessage(@Param("userEnName")String userEnName, @Param("start")Integer start, @Param("pageSize")Integer pageSize);

    Integer queryMsgCountByUserEnName(@Param("userEnName")String userEnName);

    Integer updateOneMsgByMsgId(@Param("messageId")Integer messageId);

    Integer updateAllMsgByUserEnName(@Param("userEnName")String userEnName);

    void addMsgFromFdev(MessageFdev messageFdev);

    List<MessageFdev> queryMessageRecord(@Param("userEnName")String userEnName, @Param("start")Integer start, @Param("pageSize")Integer pageSize, @Param("messageFlag")String messageFlag);

    Integer queryMessageRecordCount(@Param("userEnName") String userEnName, @Param("messageFlag") String messageFlag);

    List<MessageFdev> queryMasterMessageRecord(Integer start, Integer pageSize, String messageFlag);

    Integer queryMasterMessageRecordCount(@Param("messageFlag") String messageFlag);

    List<String> queryTestDesc(@Param("workNo")String workNo);

    String queryFirstTestDesc(@Param("workNo")String workNo);

    List<MessageFdev> queryByTaskNo(@Param("taskNo") String taskNo, String orderType);

    Integer updateWorkNoByMsgId(@Param("workNo")String workNo,@Param("messageId")Integer messageId);

    Integer updateWorkNoByTaskNos(@Param("taskIds")List<String> taskIds,@Param("newWorkNo")String newWorkNo);
}
