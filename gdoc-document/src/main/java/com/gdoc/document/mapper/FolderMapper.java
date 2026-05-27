package com.gdoc.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocFolder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FolderMapper extends BaseMapper<GdocFolder> {
}