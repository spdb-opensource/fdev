package com.spdb.fdev.freport.spdb.dto.gitlab;

import lombok.Data;

@Data
public class WebHooksDto {

    private String object_kind;//对象类型

    private String event_type;//事件类型

    private Project project;//项目信息

    private Repository repository;//仓库信息

    private User user;//用户信息

    private ObjectAttributes objectattributes;//对象属性
}
