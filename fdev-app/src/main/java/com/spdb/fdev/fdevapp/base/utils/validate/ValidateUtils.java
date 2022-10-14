package com.spdb.fdev.fdevapp.base.utils.validate;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.dict.Dict;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/5/16 17:14
 */
public class ValidateUtils {

    /**
     * 判断对象是否为空
     * @param obj
     * @param message
     */
    public static void validateObj(Object obj, String message) {
        if (null == obj) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{message});
        }
    }

    /**
     * 对比分支
     * @param branchList
     */
    public static void compireBranch(Object branchList) {
        ArrayList parseArray = (ArrayList) branchList;
        if (!CommonUtils.isNullOrEmpty(parseArray)) {
            for (Object Branch : parseArray) {
                Map map = (Map) Branch;
                if (Dict.SIT_UP.equals(map.get(Dict.NAME))) {// 比对分支名
                    throw new FdevException(ErrorConstants.GITLAB_SERVER_ERROR, new String[]{"SIT分支已经存在,请先删除"});
                }
            }
        }
    }

}
