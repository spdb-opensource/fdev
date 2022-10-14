package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.spdb.entity.component.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author liux81
 * @DATE 2022/1/25
 */
@Component
public class ComponentDao extends BaseDao{

    /**
     * 查询所有后端组件
     * @return
     */
    public List<ComponentEntity> findAllComponent(){
        return getMongoTempate(MongoConstant.COMPONENT).findAll(ComponentEntity.class);
    }

    /**
     * 查询所有前端组件
     * @return
     */
    public List<MpassComponentEntity> findAllMpaasComponent(){
        return getMongoTempate(MongoConstant.COMPONENT).findAll(MpassComponentEntity.class);
    }

    /**
     * 查询所有后端骨架
     * @return
     */
    public List<ArchetypeEntity> findAllArche(){
        return getMongoTempate(MongoConstant.COMPONENT).findAll(ArchetypeEntity.class);
    }

    /**
     * 查询所有前端骨架
     * @return
     */
    public List<MpassArchetypeEntity> findAllMpaasArche(){
        return getMongoTempate(MongoConstant.COMPONENT).findAll(MpassArchetypeEntity.class);
    }

    /**
     * 查询所有镜像
     * @return
     */
    public List<BaseImageEntity> findAllImage(){
        return getMongoTempate(MongoConstant.COMPONENT).findAll(BaseImageEntity.class);
    }
}
