package com.gdoc.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.document.mapper.FolderMapper;
import com.gdoc.model.dto.FolderCreateRequest;
import com.gdoc.model.dto.FolderVO;
import com.gdoc.model.entity.GdocFolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final FolderMapper folderMapper;

    public FolderService(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    public FolderVO create(Long ownerId, FolderCreateRequest request) {
        GdocFolder folder = new GdocFolder();
        folder.setName(request.getName());
        folder.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        folder.setOwnerId(ownerId);
        folder.setSortOrder(0);
        folderMapper.insert(folder);

        FolderVO vo = new FolderVO();
        vo.setId(folder.getId());
        vo.setName(folder.getName());
        vo.setParentId(folder.getParentId());
        return vo;
    }

    public List<FolderVO> list(Long ownerId) {
        List<GdocFolder> all = folderMapper.selectList(
                new LambdaQueryWrapper<GdocFolder>().eq(GdocFolder::getOwnerId, ownerId)
                        .orderByAsc(GdocFolder::getSortOrder));

        Map<Long, List<FolderVO>> parentMap = all.stream()
                .map(f -> {
                    FolderVO vo = new FolderVO();
                    vo.setId(f.getId());
                    vo.setName(f.getName());
                    vo.setParentId(f.getParentId());
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.groupingBy(FolderVO::getParentId));

        List<FolderVO> roots = parentMap.getOrDefault(0L, new ArrayList<>());

        for (FolderVO root : roots) {
            fillChildren(root, parentMap);
        }

        return roots;
    }

    private void fillChildren(FolderVO parent, Map<Long, List<FolderVO>> parentMap) {
        List<FolderVO> children = parentMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (FolderVO child : children) {
            fillChildren(child, parentMap);
        }
    }

    public void rename(Long folderId, Long ownerId, String name) {
        GdocFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此文件夹");
        }
        folder.setName(name);
        folderMapper.updateById(folder);
    }

    public void delete(Long folderId, Long ownerId) {
        GdocFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此文件夹");
        }
        folderMapper.deleteById(folderId);
        // Move subfolders to root
        LambdaQueryWrapper<GdocFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GdocFolder::getParentId, folderId);
        List<GdocFolder> children = folderMapper.selectList(wrapper);
        children.forEach(c -> {
            c.setParentId(0L);
            folderMapper.updateById(c);
        });
    }
}