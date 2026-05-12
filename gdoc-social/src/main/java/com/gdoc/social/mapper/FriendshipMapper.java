package com.gdoc.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocFriendship;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendshipMapper extends BaseMapper<GdocFriendship> {
}
