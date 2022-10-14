package com.spdb.fdev.pipeline.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.pipeline.dao.IImageDao;
import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.entity.Images;
import com.spdb.fdev.pipeline.service.IImageService;
import com.spdb.fdev.pipeline.service.IUserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ImageServiceImpl implements IImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    @Autowired
    private IImageDao imageDao;
    @Autowired
    private IUserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${pipelineTemplate.white.list:}")
    private List<String> whiteList;

    @Value("${group.role.admin.id}")
    private String groupRoleAdminId;

    public Page<? extends Map> queryAllImages(JsonNode jsonNode) throws Exception {
        int pageSize = Optional.ofNullable(jsonNode.get(Dict.PAGESIZE)).map(JsonNode::asInt).orElse(0);
        int pageNum = Optional.ofNullable(jsonNode.get(Dict.PAGE_NUM)).map(JsonNode::asInt).orElse(0);
        PageRequest pageRequest = null;
        if (pageSize != 0) {
            pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, Dict._ID);
        }
        Query query = new Query();
        return queryImagesVisible(query, pageRequest);
    }

    private Page<Map> queryImagesVisible(Query query, Pageable pageable) throws Exception {
        User user = userService.getUserFromRedis();
        Criteria statusCriteria = Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN);
        query.addCriteria(statusCriteria);
        //非管理员用户
        boolean isAdmin = Dict.ADMIN.equals(user.getUser_name_en());
        boolean isInWhiteList = whiteList.contains(user.getUser_name_en());
        if (!isAdmin && !isInWhiteList) {
//            Criteria groupsCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.GROUP).and(Dict.ACTIVE).is(true);
            Criteria visibleRangeCriteria = Criteria.where(Dict.VISIBLERANGE).is(Dict.PUBLIC);
            Criteria userCriteria = Criteria.where(Dict.AUTHOR__ID).is(user.getId());
//            String groupId = user.getGroup_id();
            //根据组id查询当前组和其所有子组
            //List<String> groupIds = appService.queryCurrentAndChildGroup(groupId);
//            List<String> groupIds = userService.getChildGroupIdsByGroupId(groupId);
//            if (!CommonUtils.isNullOrEmpty(groupIds)) {
//                groupsCriteria.and(Dict.GROUPID).in(groupIds);
//            }
            query.addCriteria(new Criteria().orOperator(/*groupsCriteria,*/ visibleRangeCriteria, userCriteria));
        }
        Page<Map> imagesPage = imageDao.queryImagesAsMap(query, pageable);
        imagesPage.getContent().forEach(image -> {
            if (isAdmin || isInWhiteList || user.getRole_id().contains(groupRoleAdminId) || image.get(Dict.AUTHOR) != null &&
                    Objects.equals(((Map) image.get(Dict.AUTHOR)).get(Dict.ID), user.getId())) {
                image.put(Dict.CANEDIT, String.valueOf(1));
            } else {
                image.put(Dict.CANEDIT, String.valueOf(0));
            }
            image.put(Dict.ACTIVE, Objects.equals(image.get(Dict.VISIBLERANGE), Dict.PUBLIC));
            image.remove(Dict._ID);
        });
        return imagesPage;
    }

    @Override
    public Map addImage(Images image) throws Exception {
        image.setId(new ObjectId().toString());
        image.setStatus(Constants.STATUS_OPEN);
        Author author = userService.getAuthor();
        if (Dict.ADMIN.equals(author.getNameEn())) {
            Author adminAuthor = new Author();
            adminAuthor.setId(Dict.SYSTEM);
            adminAuthor.setNameCn(Dict.SYSTEM);
            adminAuthor.setNameEn(Dict.SYSTEM);
            image.setVisibleRange(Dict.PUBLIC);
            image.setAuthor(adminAuthor);
        } else {
            if (CommonUtils.isNullOrEmpty(image.getVisibleRange()))
                image.setVisibleRange(Dict.PUBLIC);
            image.setAuthor(author);
            image.setGroupId(userService.getUserFromRedis().getGroup_id());
        }
        Map map = imageDao.saveAsMap(image);
        map.remove(Dict._ID);
        map.put(Dict.CANEDIT, String.valueOf(1));
        map.put(Dict.ACTIVE, Objects.equals(map.get(Dict.VISIBLERANGE), Dict.PUBLIC));
        return map;
    }

    @Override
    public Map updateImage(Map requestParam) throws Exception {
        User user = userService.getUserFromRedis();
        Images images_exits = imageDao.findImageById((String) requestParam.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(images_exits)) {
            logger.error("***************** images is not exits *************");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"images is not exits"});
        }
        Author currentAuthor = userService.getAuthor();
        String author_id = images_exits.getAuthor().getId();
        String currAuthorId = currentAuthor.getId();
        //如果这个镜像是用户创建的，则可以编辑这个镜像,或者管理员可以更新这个镜像
        if (currAuthorId.equals(author_id)
                || Dict.ADMIN.equals(currentAuthor.getNameEn())
                || whiteList.contains(currentAuthor.getNameEn())
                || user.getRole_id().contains(groupRoleAdminId)) {
            //按条件更新
            if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.NAME))) {
                images_exits.setName((String) requestParam.get(Dict.NAME));
            }
            if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.PATH))) {
                images_exits.setPath((String) requestParam.get(Dict.PATH));
            }
            if (requestParam.containsKey(Dict.DESC)) {
                images_exits.setDesc((String) requestParam.get(Dict.DESC));
            }
            if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.VISIBLERANGE))) {
                images_exits.setVisibleRange((String) requestParam.get(Dict.VISIBLERANGE));
            }
            if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.STATUS))) {
                images_exits.setStatus((String) requestParam.get(Dict.STATUS));
            }
            if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ACTIVE))) {
                images_exits.setVisibleRange((boolean) requestParam.get(Dict.ACTIVE) ? Dict.PUBLIC : Dict.PRIVATE);
            }
            Map imageUpdated = imageDao.saveAsMap(images_exits);
            imageUpdated.remove(Dict._ID);
            imageUpdated.put(Dict.CANEDIT, String.valueOf(1));
            imageUpdated.put(Dict.ACTIVE, Objects.equals(imageUpdated.get(Dict.VISIBLERANGE), Dict.PUBLIC));
            return imageUpdated;
        }
        logger.error("**********user has not permission to retry");
        throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"current user illegal permission"});
    }

    @Override
    public Images findImageById(String id) {
        return imageDao.findImageById(id);
    }
}
