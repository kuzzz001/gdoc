package com.gdoc.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<GdocNotification> {
}