package com.spdb.fdev.fuser.spdb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/9/29
 */
@Document(collection = "menu")
@ApiModel(value = "菜单")
public class Menu implements Serializable {

    private static final long serialVersionUID = 2327139297301528549L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("menuId")
    @ApiModelProperty(value = "菜单id")
    private String menuId;

    @Field("nameEn")
    @ApiModelProperty(value = "菜单英文名称")
    private String nameEn;

    @Field("nameCn")
    @ApiModelProperty(value = "菜单中文名称")
    private String nameCn;

    @Field("path")
    @ApiModelProperty(value = "菜单路径")
    private String path;

    @Field("sort")
    @ApiModelProperty(value = "序号")
    private String sort;

    @Field("parentId")
    @ApiModelProperty(value = "父id")
    private String parentId;

    @Field("level")
    @ApiModelProperty(value = "菜单级别")
    private String level;

    @Field("menuType")
    @ApiModelProperty(value = "菜单类型:1.非敏,2.敏捷团队")
    private List<String> menuType;

    private List<Menu> childrenList;

    public Menu() {

    }

    public Menu(String menuId, String nameEn, String nameCn, String path, String sort, String parentId, String level, List<String> menuType, List<Menu> childrenList) {
        this.menuId = menuId;
        this.nameEn = nameEn;
        this.nameCn = nameCn;
        this.path = path;
        this.sort = sort;
        this.parentId = parentId;
        this.level = level;
        this.menuType = menuType;
        this.childrenList = childrenList;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getMenuType() {
        return menuType;
    }

    public void setMenuType(List<String> menuType) {
        this.menuType = menuType;
    }

    public List<Menu> getChildrenList() {
        return childrenList;
    }

    public void setChildrenList(List<Menu> childrenList) {
        this.childrenList = childrenList;
    }


    //存数据时，初始化_id和id
    public void initId() {
        ObjectId temp = new ObjectId();
        this._id = temp;
    }

    public Integer getIntSort() {
        return Integer.valueOf(this.sort);
    }
}

