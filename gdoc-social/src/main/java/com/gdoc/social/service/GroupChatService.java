package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.model.entity.GdocGroupChat;
import com.gdoc.model.entity.GdocGroupMember;
import com.gdoc.model.entity.GdocGroupMessage;
import com.gdoc.social.mapper.GroupChatMapper;
import com.gdoc.social.mapper.GroupMemberMapper;
import com.gdoc.social.mapper.GroupMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GroupChatService {

    private final GroupChatMapper groupChatMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMessageMapper groupMessageMapper;

    public GroupChatService(GroupChatMapper groupChatMapper, GroupMemberMapper groupMemberMapper,
                            GroupMessageMapper groupMessageMapper) {
        this.groupChatMapper = groupChatMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.groupMessageMapper = groupMessageMapper;
    }

    @Transactional
    public Map<String, Object> create(Long ownerId, String name) {
        GdocGroupChat group = new GdocGroupChat();
        group.setName(name);
        group.setOwnerId(ownerId);
        groupChatMapper.insert(group);

        GdocGroupMember member = new GdocGroupMember();
        member.setGroupId(group.getId());
        member.setUserId(ownerId);
        member.setRole("admin");
        groupMemberMapper.insert(member);

        return toMap(group);
    }

    public List<Map<String, Object>> list(Long userId) {
        List<GdocGroupMember> memberships = groupMemberMapper.selectList(
                new LambdaQueryWrapper<GdocGroupMember>().eq(GdocGroupMember::getUserId, userId));

        List<Map<String, Object>> groups = new ArrayList<>();
        for (GdocGroupMember m : memberships) {
            GdocGroupChat group = groupChatMapper.selectById(m.getGroupId());
            if (group != null) {
                Map<String, Object> map = toMap(group);
                map.put("role", m.getRole());
                groups.add(map);
            }
        }
        return groups;
    }

    @Transactional
    public void addMember(Long groupId, Long userId) {
        boolean exists = groupMemberMapper.exists(new LambdaQueryWrapper<GdocGroupMember>()
                .eq(GdocGroupMember::getGroupId, groupId)
                .eq(GdocGroupMember::getUserId, userId));
        if (!exists) {
            GdocGroupMember member = new GdocGroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole("member");
            groupMemberMapper.insert(member);
        }
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        groupMemberMapper.delete(new LambdaQueryWrapper<GdocGroupMember>()
                .eq(GdocGroupMember::getGroupId, groupId)
                .eq(GdocGroupMember::getUserId, userId));
    }

    @Transactional
    public void sendMessage(Long groupId, Long senderId, String content) {
        GdocGroupMessage msg = new GdocGroupMessage();
        msg.setGroupId(groupId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setMsgType("text");
        groupMessageMapper.insert(msg);
    }

    public List<Map<String, Object>> getMessages(Long groupId, int limit) {
        List<GdocGroupMessage> messages = groupMessageMapper.selectList(
                new LambdaQueryWrapper<GdocGroupMessage>()
                        .eq(GdocGroupMessage::getGroupId, groupId)
                        .orderByDesc(GdocGroupMessage::getCreatedAt)
                        .last("LIMIT " + limit));

        Collections.reverse(messages);
        List<Map<String, Object>> result = new ArrayList<>();
        for (GdocGroupMessage m : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("groupId", m.getGroupId());
            map.put("senderId", m.getSenderId());
            map.put("content", m.getContent());
            map.put("msgType", m.getMsgType());
            map.put("createdAt", m.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    private Map<String, Object> toMap(GdocGroupChat group) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", group.getId());
        map.put("name", group.getName());
        map.put("avatarUrl", group.getAvatarUrl());
        map.put("ownerId", group.getOwnerId());
        return map;
    }
}