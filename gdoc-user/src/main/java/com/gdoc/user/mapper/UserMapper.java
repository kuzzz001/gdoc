package com.gdoc.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<GdocUser> {

    @Select("SELECT MAX(CAST(username AS UNSIGNED)) FROM gdoc_user")
    Integer selectMaxAccountNo();
}
