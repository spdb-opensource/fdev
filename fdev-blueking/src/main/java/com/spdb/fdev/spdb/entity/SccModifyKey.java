package com.spdb.fdev.spdb.entity;

import com.spdb.fdev.base.dict.Dict;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:03
 * @Description: scc modifyKey  实体类
 */
@Document(collection = Dict.SCCMODIFYKEY)
public class SccModifyKey {

    private static final long serialVersionUID = 8637314009320661716L;

    @Field("yaml")
    private String yaml;

    @Field("namespace_code")
    private String namespace_code;

    @Field("resource_code")
    private String resource_code;

    public SccModifyKey(){

    }

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }

    public String getNamespace_code() {
        return namespace_code;
    }

    public void setNamespace_code(String namespace_code) {
        this.namespace_code = namespace_code;
    }

    public String getResource_code() {
        return resource_code;
    }

    public void setResource_code(String resource_code) {
        this.resource_code = resource_code;
    }

    @Override
    public String toString() {
        return "SccModifyKey{" +
                "yaml='" + yaml + '\'' +
                ", namespace_code='" + namespace_code + '\'' +
                ", resource_code='" + resource_code + '\'' +
                '}';
    }
}
