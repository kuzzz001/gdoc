package com.gdoc.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdoc.history.entity.GdocSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SnapshotMapper extends BaseMapper<GdocSnapshot> {
}
