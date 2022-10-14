package com.spdb.fdev.base.listener;

import com.spdb.fdev.base.annotation.nonull.AutoInc;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.entity.SequenceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

@Component
public class SaveMongoEventListener extends AbstractMongoEventListener<Object> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event){
        final Object source = event.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    if(field.isAnnotationPresent(AutoInc.class)){
                        field.set(source, getNextConfigId(source.getClass().getSimpleName()));
                    }
                }
            });
        }
    }

    private Long getNextConfigId(String collName){
        Update update = new Update();
        update.inc("seqId",1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Query query = new Query(Criteria.where("collName").is(collName));
        SequenceId seq = mongoTemplate.findAndModify(query, update, options, SequenceId.class);
        return seq.getSeqId();
    }

    /**
     * 获取pipelineNum的当前值
     *
     * @return
     */
    public Long getPipelineNum() {
        Query query = new Query(Criteria.where("collName").is("PipelineExe"));
        List<SequenceId> sequenceId = this.mongoTemplate.find(query,SequenceId.class, "sequenceId");
        //修复生产上没有跑过流水线而不会产生SequenceId这个表的空指针问题
        if (CommonUtils.isNullOrEmpty(sequenceId))
            return 0L;
        else
            return sequenceId.get(0).getSeqId();
    }
}
