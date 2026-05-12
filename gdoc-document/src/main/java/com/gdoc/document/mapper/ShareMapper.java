package com.gdoc.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocShare;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShareMapper extends BaseMapper<GdocShare> {
}
