package com.gdoc.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.model.entity.GdocCollaborator;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CollaboratorMapper extends BaseMapper<GdocCollaborator> {
}
