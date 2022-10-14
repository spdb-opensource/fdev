package com.spdb.fdev.fuser.spdb.service;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fuser.spdb.entity.user.UserCommissionEvent;

public interface CommissionEventService {
	
	/**
	 * 添加代办事项
	 * @param userCommissionEvent
	 * @return 代办事项
	 */
	UserCommissionEvent addCommissionEvent(UserCommissionEvent userCommissionEvent)throws Exception;

	/**
	 * 根据代办事项id更新代办事项
	 * 
	 * @param userCommissionEvent
	 * @return
	 */
	UserCommissionEvent updateByTargetIdAndType(UserCommissionEvent userCommissionEvent)throws Exception;
	
	/**
	 * 根据目标id和代办类型查询代办事项明细
	 * @param target_id
	 * @param type
	 * @return
	 * @throws Exception
	 */
	UserCommissionEvent queryDetailBytargetIdAndType(String target_id,String type,String module)throws Exception;

	/**
	 * 根据用户id查询相关代办事项列表
	 * @param userCommissionEvent
	 * @return
	 */
	List<UserCommissionEvent> queryCommissionEvent(UserCommissionEvent userCommissionEvent)throws Exception;

	/**
	 * 根据用户iD和代办状态查询代办事项列表
	 * @param userCommissionEvent
	 * @return
	 */
	List<UserCommissionEvent> queryCommissionEventByStatus(UserCommissionEvent userCommissionEvent)throws Exception;

	/**
	 *  根据代办事项id查询代办事项
	 * @param id
	 * @return
	 */
	UserCommissionEvent queryEventById(String id)throws Exception;

	/**
	 * 根据代办事项修改标签 todo 或  done
	 * @param newEvent
	 * @return
	 */
	UserCommissionEvent updateLabelById(UserCommissionEvent newEvent)throws Exception;
	
	/**
	 * 根据todo或done统计当前用户的label数量 
	 * @param id
	 * @param Label
	 */
	Long countLabelNum(List<String> userIds, String label)throws Exception;

	/**
	 * 统计当前用户的label总数量
	 * @param user_list
	 * @return
	 */
	Long totalLabelNum(List<String> user_list)throws Exception;

	/**
	 * 数据存在就修改,不存在就新增
	 * @param commissionEvent
	 * @param userCommissionEvent
	 * @return
	 * @throws Exception
	 */
	UserCommissionEvent upsertCommissionEvent(UserCommissionEvent commissionEvent,
			UserCommissionEvent userCommissionEvent)throws Exception;

	/**
	 * 删除指定待办事项
	 * @param id
	 * @return
	 */
	Long deleteCommissionEventById(String id);
	
	/**
	 * 批量删除1个月前状态为：已解决或DONE的代办项
	 * @return
	 */
	List<UserCommissionEvent> queryListByStatusOrLabel(UserCommissionEvent userCommissionEvent)throws Exception;

}
