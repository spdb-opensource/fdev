package com.spdb.fdev.base.utils.validate;


import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.entity.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2019/5/16 17:00
 */
@Component
public class ValidateComponent {

    /**
     * 检验 组件 英文，中文名不能重复
     *
     * @param query
     * @param componentInfo
     */
    public static void checkAppNameEnAndNameZh(List<ComponentInfo> query, ComponentInfo componentInfo) {
        for (ComponentInfo entity : query) {
            if (entity.getName_en().equals(componentInfo.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前组件英文名已存在"});
            }
            if (entity.getName_cn().equals(componentInfo.getName_cn())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前组件中文已存在"});
            }
        }
    }

    /**
     * 检验 骨架 英文，中文名不能重复
     *
     * @param query
     * @param archetypeInfo
     */
    public static void checkAppNameEnAndNameZh(List<ArchetypeInfo> query, ArchetypeInfo archetypeInfo) {
        for (ArchetypeInfo entity : query) {
            if (entity.getName_en().equals(archetypeInfo.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前骨架英文名已存在"});
            }
            if (entity.getName_cn().equals(archetypeInfo.getName_cn())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前骨架中文已存在"});
            }
        }
    }

    /**
     * 检验 镜像 英文，中文名不能重复
     *
     * @param query
     * @param baseImageInfo
     */
    public static void checkAppNameEnAndNameZh(List<BaseImageInfo> query, BaseImageInfo baseImageInfo) {
        for (BaseImageInfo entity : query) {
            if (entity.getName().equals(baseImageInfo.getName())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前镜像英文名已存在"});
            }
            if (entity.getName_cn().equals(baseImageInfo.getName_cn())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前镜像中文已存在"});
            }
        }
    }

    /**
     * 检验mpass组件英文，中文名不能重复
     *
     * @param query
     * @param mpassComponent
     */
    public static void checkAppNameEnAndNameZh(List<MpassComponent> query, MpassComponent mpassComponent) {
        for (MpassComponent entity : query) {
            if (entity.getName_en().equals(mpassComponent.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前组件英文名已存在"});
            }
            if (entity.getName_cn().equals(mpassComponent.getName_cn())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前组件中文已存在"});
            }
        }
    }

    /**
     * 检验 mpass骨架 英文，中文名不能重复
     *
     * @param query
     * @param mpassArchetype
     */
    public static void checkAppNameEnAndNameZh(List<MpassArchetype> query, MpassArchetype mpassArchetype) {
        for (MpassArchetype entity : query) {
            if (entity.getName_en().equals(mpassArchetype.getName_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前骨架英文名已存在"});
            }
            if (entity.getName_cn().equals(mpassArchetype.getName_cn())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前骨架中文已存在"});
            }
        }
    }



    /**
     * 校验组件英文名格式
     *
     * @param enName
     * @return
     */
    public static boolean validateEnName(String enName) {
        if (StringUtils.isBlank(enName))
            return false;
        Pattern humpPattern = Pattern.compile("^[a-z][a-z0-9-]*[a-z]$");
        Matcher matcher = humpPattern.matcher(enName);
        if (!matcher.find()) {
            return false;
        }
        int count = 0;
        Pattern pattern = Pattern.compile("-");
        matcher = pattern.matcher(enName);
        while (matcher.find()) {
            count++;
        }
        if (count != 2 || enName.contains("--")) {
            return false;
        }
        return true;
    }
}
