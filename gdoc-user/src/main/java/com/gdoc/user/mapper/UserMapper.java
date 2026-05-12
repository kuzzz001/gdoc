package com.gdoc.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<GdocUser> {
}
