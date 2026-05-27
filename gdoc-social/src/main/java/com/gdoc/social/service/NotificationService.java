package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.model.dto.NotificationVO;
import com.gdoc.model.entity.GdocNotification;
import com.gdoc.social.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void send(Long userId, String type, String content, Long relatedId) {
        GdocNotification n = new GdocNotification();
        n.setUserId(userId);
        n.setType(type);
        n.setContent(content);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    public List<NotificationVO> list(Long userId) {
        return notificationMapper.selectList(
                new LambdaQueryWrapper<GdocNotification>()
                        .eq(GdocNotification::getUserId, userId)
                        .orderByDesc(GdocNotification::getCreatedAt))
                .stream().map(n -> {
                    NotificationVO vo = new NotificationVO();
                    vo.setId(n.getId());
                    vo.setType(n.getType());
                    vo.setContent(n.getContent());
                    vo.setRelatedId(n.getRelatedId());
                    vo.setIsRead(n.getIsRead());
                    vo.setCreatedAt(n.getCreatedAt());
                    return vo;
                }).collect(Collectors.toList());
    }

    public int unreadCount(Long userId) {
        return Math.toIntExact(notificationMapper.selectCount(
                new LambdaQueryWrapper<GdocNotification>()
                        .eq(GdocNotification::getUserId, userId)
                        .eq(GdocNotification::getIsRead, 0)));
    }

    public void markRead(Long notificationId, Long userId) {
        GdocNotification n = notificationMapper.selectById(notificationId);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void markAllRead(Long userId) {
        List<GdocNotification> unread = notificationMapper.selectList(
                new LambdaQueryWrapper<GdocNotification>()
                        .eq(GdocNotification::getUserId, userId)
                        .eq(GdocNotification::getIsRead, 0));
        unread.forEach(n -> {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        });
    }
}