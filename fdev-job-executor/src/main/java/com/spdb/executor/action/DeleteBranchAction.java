package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.csii.pe.spdb.common.util.CommonUtils;
import com.spdb.executor.service.DeleteBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
public class DeleteBranchAction extends BaseExecutableAction {

    @Autowired
    private DeleteBranchService deleteBranchService;


    @Value("${delete.branch.projectids:}")
    private String projectIds;


    @Override
    public void execute(Context context) {
        logger.info("execute DeleteBranch begin");
        /**
         * 清除gitlab上对应的分支
         */
        if (!CommonUtils.isNullOrEmpty(projectIds)) {
            if (projectIds.equals(Dict.ALL)) {
                deleteBranchService.deleteBranchAndTag(null);
            } else {
                String[] ids = projectIds.split(",");
                for (String id : ids) {
                    deleteBranchService.deleteBranchAndTag(id);
                }
            }
        }
        logger.info("execute DeleteBranch end");
    }
}
