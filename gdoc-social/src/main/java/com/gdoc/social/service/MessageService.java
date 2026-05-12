package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.model.dto.MessageVO;
import com.gdoc.model.dto.SendMessageRequest;
import com.gdoc.model.entity.GdocMessage;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.social.mapper.MessageMapper;
import com.gdoc.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    public MessageService(MessageMapper messageMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    public MessageVO sendMessage(Long senderId, SendMessageRequest request) {
        GdocUser receiver = userMapper.selectById(request.getReceiverId());
        if (receiver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        GdocMessage message = new GdocMessage();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType() != null ? request.getMsgType() : GdocMessage.TYPE_TEXT);
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setStatus(GdocMessage.STATUS_SENT);
        messageMapper.insert(message);

        return toVO(message);
    }

    public List<MessageVO> getChatHistory(Long userId, Long friendId, int limit, int offset) {
        List<GdocMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<GdocMessage>()
                .and(w -> w.eq(GdocMessage::getSenderId, userId).eq(GdocMessage::getReceiverId, friendId))
                .or(w -> w.eq(GdocMessage::getSenderId, friendId).eq(GdocMessage::getReceiverId, userId))
                .orderByDesc(GdocMessage::getCreatedAt)
                .last("LIMIT " + limit + " OFFSET " + offset));

        markAsDelivered(userId, friendId);

        return messages.stream().map(this::toVO).toList();
    }

    public void markAsDelivered(Long receiverId, Long senderId) {
        messageMapper.update(null, new LambdaUpdateWrapper<GdocMessage>()
                .eq(GdocMessage::getReceiverId, receiverId)
                .eq(GdocMessage::getSenderId, senderId)
                .eq(GdocMessage::getStatus, GdocMessage.STATUS_SENT)
                .set(GdocMessage::getStatus, GdocMessage.STATUS_DELIVERED));
    }

    public void markAsRead(Long receiverId, Long senderId) {
        messageMapper.update(null, new LambdaUpdateWrapper<GdocMessage>()
                .eq(GdocMessage::getReceiverId, receiverId)
                .eq(GdocMessage::getSenderId, senderId)
                .lt(GdocMessage::getStatus, GdocMessage.STATUS_READ)
                .set(GdocMessage::getStatus, GdocMessage.STATUS_READ));
    }

    public int getUnreadCount(Long receiverId, Long senderId) {
        return Math.toIntExact(messageMapper.selectCount(new LambdaQueryWrapper<GdocMessage>()
                .eq(GdocMessage::getReceiverId, receiverId)
                .eq(GdocMessage::getSenderId, senderId)
                .lt(GdocMessage::getStatus, GdocMessage.STATUS_READ)));
    }

    public int getTotalUnreadCount(Long receiverId) {
        return Math.toIntExact(messageMapper.selectCount(new LambdaQueryWrapper<GdocMessage>()
                .eq(GdocMessage::getReceiverId, receiverId)
                .lt(GdocMessage::getStatus, GdocMessage.STATUS_READ)));
    }

    private MessageVO toVO(GdocMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setMsgType(message.getMsgType());
        vo.setFileUrl(message.getFileUrl());
        vo.setFileName(message.getFileName());
        vo.setFileSize(message.getFileSize());
        vo.setStatus(message.getStatus());
        vo.setCreatedAt(message.getCreatedAt());

        GdocUser sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            vo.setSenderAvatar(sender.getAvatarUrl());
        }
        return vo;
    }
}
