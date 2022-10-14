package com.spdb.fdev.freport.spdb.entity.release;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "release_tasks")
@ApiModel(value = "投产任务")
@CompoundIndexes({@CompoundIndex(name = "rls_task_idx", def = "{'release_node_name': 1, 'task_id': 1}", unique = true)})
public class ReleaseTask {

    @Field("task_id")
    @ApiModelProperty(value = "任务id")
    private String taskId;

    @Field("task_status")
    @ApiModelProperty(value = "任务审核状态")
    private String taskStatus;

    @Field("release_node_name")
    @ApiModelProperty(value = "投产点名称")
    private String releaseNodeName;

    @Field("application_id")
    @ApiModelProperty(value = "应用编号")
    private String applicationId;

    @Field("reject_reason")
    @ApiModelProperty(value = "拒接理由")
    private String rejectReason;

    @Field("type")
    @ApiModelProperty(value = "投产窗口类型")
    private String type;

    @Field("rqrmnt_no")
    @ApiModelProperty(value = "需求id")
    private String rqrmntNo;

}
