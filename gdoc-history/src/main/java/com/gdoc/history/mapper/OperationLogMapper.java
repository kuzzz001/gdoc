package com.gdoc.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.history.entity.GdocOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<GdocOperationLog> {
}
