package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gdoc.common.exception.BusinessException;
import com.gdoc.common.result.ResultCode;
import com.gdoc.model.dto.FriendVO;
import com.gdoc.model.dto.UserVO;
import com.gdoc.model.entity.GdocFriendship;
import com.gdoc.model.entity.GdocUser;
import com.gdoc.social.mapper.FriendshipMapper;
import com.gdoc.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendshipService {

    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;

    public FriendshipService(FriendshipMapper friendshipMapper, UserMapper userMapper) {
        this.friendshipMapper = friendshipMapper;
        this.userMapper = userMapper;
    }

    public List<UserVO> searchUsers(String keyword, Long currentUserId) {
        LambdaQueryWrapper<GdocUser> wrapper = new LambdaQueryWrapper<GdocUser>()
                .ne(GdocUser::getId, currentUserId)
                .and(w -> w.like(GdocUser::getUsername, keyword)
                        .or().like(GdocUser::getNickname, keyword))
                .last("LIMIT 20");
        List<GdocUser> users = userMapper.selectList(wrapper);
        List<UserVO> result = new ArrayList<>();
        for (GdocUser user : users) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException(ResultCode.FRIEND_CANNOT_ADD_SELF);
        }

        GdocUser friend = userMapper.selectById(friendId);
        if (friend == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        boolean exists = friendshipMapper.exists(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, userId)
                .eq(GdocFriendship::getFriendId, friendId));

        boolean reverseExists = friendshipMapper.exists(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, friendId)
                .eq(GdocFriendship::getFriendId, userId));

        if (exists || reverseExists) {
            throw new BusinessException(ResultCode.FRIEND_ALREADY_EXISTS);
        }

        GdocFriendship friendship = new GdocFriendship();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(GdocFriendship.STATUS_PENDING);
        friendshipMapper.insert(friendship);
    }

    @Transactional
    public void acceptFriend(Long userId, Long friendshipId) {
        GdocFriendship friendship = friendshipMapper.selectById(friendshipId);
        if (friendship == null) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }
        if (!friendship.getFriendId().equals(userId)) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }
        if (friendship.getStatus() != GdocFriendship.STATUS_PENDING) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_ALREADY_HANDLED);
        }

        friendship.setStatus(GdocFriendship.STATUS_ACCEPTED);
        friendshipMapper.updateById(friendship);

        GdocFriendship reverse = new GdocFriendship();
        reverse.setUserId(userId);
        reverse.setFriendId(friendship.getUserId());
        reverse.setStatus(GdocFriendship.STATUS_ACCEPTED);
        friendshipMapper.insert(reverse);
    }

    @Transactional
    public void rejectFriend(Long userId, Long friendshipId) {
        GdocFriendship friendship = friendshipMapper.selectById(friendshipId);
        if (friendship == null) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }
        if (!friendship.getFriendId().equals(userId)) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_NOT_FOUND);
        }
        if (friendship.getStatus() != GdocFriendship.STATUS_PENDING) {
            throw new BusinessException(ResultCode.FRIEND_REQUEST_ALREADY_HANDLED);
        }

        friendship.setStatus(GdocFriendship.STATUS_REJECTED);
        friendshipMapper.updateById(friendship);
    }

    public List<FriendVO> getFriendList(Long userId) {
        List<GdocFriendship> friendships = friendshipMapper.selectList(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, userId)
                .eq(GdocFriendship::getStatus, GdocFriendship.STATUS_ACCEPTED)
                .orderByDesc(GdocFriendship::getUpdatedAt));

        List<FriendVO> result = new ArrayList<>();
        for (GdocFriendship f : friendships) {
            GdocUser friend = userMapper.selectById(f.getFriendId());
            if (friend != null) {
                FriendVO vo = new FriendVO();
                vo.setFriendshipId(f.getId());
                vo.setUserId(friend.getId());
                vo.setUsername(friend.getUsername());
                vo.setNickname(friend.getNickname());
                vo.setAvatarUrl(friend.getAvatarUrl());
                vo.setStatus(f.getStatus());
                result.add(vo);
            }
        }
        return result;
    }

    public List<FriendVO> getPendingRequests(Long userId) {
        List<GdocFriendship> friendships = friendshipMapper.selectList(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getFriendId, userId)
                .eq(GdocFriendship::getStatus, GdocFriendship.STATUS_PENDING)
                .orderByDesc(GdocFriendship::getCreatedAt));

        List<FriendVO> result = new ArrayList<>();
        for (GdocFriendship f : friendships) {
            GdocUser requester = userMapper.selectById(f.getUserId());
            if (requester != null) {
                FriendVO vo = new FriendVO();
                vo.setFriendshipId(f.getId());
                vo.setUserId(requester.getId());
                vo.setUsername(requester.getUsername());
                vo.setNickname(requester.getNickname());
                vo.setAvatarUrl(requester.getAvatarUrl());
                vo.setStatus(f.getStatus());
                result.add(vo);
            }
        }
        return result;
    }

    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        friendshipMapper.delete(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, userId)
                .eq(GdocFriendship::getFriendId, friendId));
        friendshipMapper.delete(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, friendId)
                .eq(GdocFriendship::getFriendId, userId));
    }

    public boolean isFriend(Long userId, Long friendId) {
        return friendshipMapper.exists(new LambdaQueryWrapper<GdocFriendship>()
                .eq(GdocFriendship::getUserId, userId)
                .eq(GdocFriendship::getFriendId, friendId)
                .eq(GdocFriendship::getStatus, GdocFriendship.STATUS_ACCEPTED));
    }
}
