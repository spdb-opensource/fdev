package com.spdb.fdev.fdevtask.spdb.entity;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * @author patrick
 * @date 2021/3/15 上午9:58
 * @Des 属性值变更记录实体
 * 最簡單的事是堅持，最難的事還是堅持
 */
@Document(collection = "change_value_log")
public class ChangeValueLog {
    @Id
    private Object _id;
    @Field
    private String id;
    @Field
    private String taskId;
    @Field
    private String userId;
    @Field
    private String userName;
    /**
     * 转译后的名称，默认是字段名字
     */
    @Field
    private String fieldName;
    @Field
    private Object beforeValue;
    @Field
    private Object afterValue;
    @Field
    private String type;
    @Field
    private String execTime;
    @Field
    private String field;

    ChangeValueLog(final String id, final String taskId, final String userId, final String userName, final String fieldName, final Object beforeValue, final Object afterValue, final String type, final String execTime, final String field) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.userName = userName;
        this.fieldName = fieldName;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.type = type;
        this.execTime = execTime;
        this.field = field;
    }

    public ChangeValueLog() {
    }

    public static ChangeValueLogBuilder builder() {
        return new ChangeValueLogBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public Object getBeforeValue() {
        return this.beforeValue;
    }

    public Object getAfterValue() {
        return this.afterValue;
    }

    public String getType() {
        return this.type;
    }

    public String getExecTime() {
        return this.execTime;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public void setBeforeValue(final Object beforeValue) {
        this.beforeValue = beforeValue;
    }

    public void setAfterValue(final Object afterValue) {
        this.afterValue = afterValue;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setExecTime(final String execTime) {
        this.execTime = execTime;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public String toString() {
        return "ChangeValueLog(id=" + this.getId() + ", userId=" + this.getUserId() + ", userName=" + this.getUserName() + ", fieldName=" + this.getFieldName() + ", beforeValue=" + this.getBeforeValue() + ", afterValue=" + this.getAfterValue() + ", type=" + this.getType() + ", execTime=" + this.getExecTime() + ")";
    }

    public static class ChangeValueLogBuilder {
        private String id;
        private String taskId;
        private String userId;
        private String userName;
        private String fieldName;
        private Object beforeValue;
        private Object afterValue;
        private String type;
        private String execTime;
        private String field;

        ChangeValueLogBuilder() {
        }

        public ChangeValueLogBuilder id(final String id) {
            this.id = id;
            return this;
        }

        public ChangeValueLogBuilder taskId(final String taskId) {
            this.taskId = taskId;
            return this;
        }

        public ChangeValueLogBuilder userId(final String userId) {
            this.userId = userId;
            return this;
        }

        public ChangeValueLogBuilder userName(final String userName) {
            this.userName = userName;
            return this;
        }

        public ChangeValueLogBuilder fieldName(final String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public ChangeValueLogBuilder beforeValue(final Object beforeValue) {
            this.beforeValue = beforeValue;
            return this;
        }

        public ChangeValueLogBuilder afterValue(final Object afterValue) {
            this.afterValue = afterValue;
            return this;
        }

        public ChangeValueLogBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public ChangeValueLogBuilder execTime(final String execTime) {
            this.execTime = execTime;
            return this;
        }

        public ChangeValueLogBuilder field(final String field) {
            this.field = field;
            return this;
        }

        public ChangeValueLog build() {
            return new ChangeValueLog(this.id, this.taskId, this.userId, this.userName, this.fieldName, this.beforeValue, this.afterValue, this.type, this.execTime, this.field);
        }

        public String toString() {
            return "ChangeValueLog.ChangeValueLogBuilder(id=" + this.id + ", userId=" + this.userId + ", userName=" + this.userName + ", fieldName=" + this.fieldName + ", beforeValue=" + this.beforeValue + ", afterValue=" + this.afterValue + ", type=" + this.type + ", execTime=" + this.execTime + ")";
        }
    }
}
