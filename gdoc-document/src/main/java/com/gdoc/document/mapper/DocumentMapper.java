package com.gdoc.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdoc.model.entity.GdocDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentMapper extends BaseMapper<GdocDocument> {

    @Select("SELECT DISTINCT d.* FROM gdoc_document d " +
            "LEFT JOIN gdoc_collaborator c ON c.doc_id = d.id AND c.user_id = #{userId} " +
            "WHERE d.deleted = 0 AND (d.owner_id = #{userId} OR c.user_id IS NOT NULL) " +
            "ORDER BY d.updated_at DESC")
    IPage<GdocDocument> selectUserDocs(Page<GdocDocument> page, @Param("userId") Long userId);

    // Recycle bin queries bypass MyBatis Plus @TableLogic auto-filter
    @Select("SELECT * FROM gdoc_document WHERE owner_id = #{userId} AND deleted = 1 ORDER BY updated_at DESC")
    IPage<GdocDocument> selectDeletedDocs(Page<GdocDocument> page, @Param("userId") Long userId);

    @Select("SELECT * FROM gdoc_document WHERE id = #{docId} AND deleted = 1")
    GdocDocument selectDeletedById(@Param("docId") Long docId);

    @org.apache.ibatis.annotations.Update("UPDATE gdoc_document SET deleted = 0 WHERE id = #{docId} AND deleted = 1")
    int restoreDeletedDoc(@Param("docId") Long docId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM gdoc_document WHERE id = #{docId} AND deleted = 1")
    int physicalDeleteDoc(@Param("docId") Long docId);

    @org.apache.ibatis.annotations.Delete("DELETE FROM gdoc_document WHERE deleted = 1 AND owner_id = #{userId}")
    int physicalDeleteAllDeleted(@Param("userId") Long userId);
}