package com.auto.dao;

import com.auto.entity.MenuSheet;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuMapper {

    void addMenu(MenuSheet menuSheet) throws Exception;

    List<MenuSheet> queryMenuByMenuSheet(@Param("menuNo") String menuNo,
    		@Param("secondaryMenuNo") String secondaryMenuNo,
            @Param("thirdMenuNo") String thirdMenuNo,
            @Param("menuSheetId") String menuSheetId) throws Exception;

    List<MenuSheet> queryMenu(@Param("search") String search, @Param("valid") String valid) throws Exception;

    void deleteMenu(@Param("menuSheetId") String menuSheetId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateMenu(@Param("menuSheetId") String menuSheetId, @Param("menuName") String menuName,
                    @Param("menuNo") String menuNo, @Param("secondaryMenu") String secondaryMenu,
                    @Param("secondaryMenuNo") String secondaryMenuNo, @Param("thirdMenu") String thirdMenu,
                    @Param("thirdMenuNo") String thirdMenuNo, @Param("userNameEn") String userNameEn,
                    @Param("time") String time) throws Exception;
}