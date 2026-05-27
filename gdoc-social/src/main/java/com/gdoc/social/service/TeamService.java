package com.gdoc.social.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdoc.model.entity.GdocTeam;
import com.gdoc.model.entity.GdocTeamMember;
import com.gdoc.social.mapper.TeamMapper;
import com.gdoc.social.mapper.TeamMemberMapper;
import com.gdoc.user.mapper.UserMapper;
import com.gdoc.model.entity.GdocUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamService {

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;

    public TeamService(TeamMapper teamMapper, TeamMemberMapper teamMemberMapper, UserMapper userMapper) {
        this.teamMapper = teamMapper;
        this.teamMemberMapper = teamMemberMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public Map<String, Object> create(Long ownerId, String name, String description) {
        GdocTeam team = new GdocTeam();
        team.setName(name);
        team.setDescription(description);
        team.setOwnerId(ownerId);
        teamMapper.insert(team);

        // Add owner as admin
        GdocTeamMember member = new GdocTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(ownerId);
        member.setRole("admin");
        teamMemberMapper.insert(member);

        return toMap(team);
    }

    public List<Map<String, Object>> list(Long userId) {
        List<GdocTeamMember> memberships = teamMemberMapper.selectList(
                new LambdaQueryWrapper<GdocTeamMember>().eq(GdocTeamMember::getUserId, userId));

        List<Map<String, Object>> teams = new ArrayList<>();
        for (GdocTeamMember m : memberships) {
            GdocTeam team = teamMapper.selectById(m.getTeamId());
            if (team != null) {
                Map<String, Object> map = toMap(team);
                map.put("role", m.getRole());
                teams.add(map);
            }
        }
        return teams;
    }

    @Transactional
    public void addMember(Long teamId, Long userId, String role) {
        boolean exists = teamMemberMapper.exists(new LambdaQueryWrapper<GdocTeamMember>()
                .eq(GdocTeamMember::getTeamId, teamId)
                .eq(GdocTeamMember::getUserId, userId));
        if (!exists) {
            GdocTeamMember member = new GdocTeamMember();
            member.setTeamId(teamId);
            member.setUserId(userId);
            member.setRole(role);
            teamMemberMapper.insert(member);
        }
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        teamMemberMapper.delete(new LambdaQueryWrapper<GdocTeamMember>()
                .eq(GdocTeamMember::getTeamId, teamId)
                .eq(GdocTeamMember::getUserId, userId));
    }

    public List<Map<String, Object>> listMembers(Long teamId) {
        List<GdocTeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<GdocTeamMember>().eq(GdocTeamMember::getTeamId, teamId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GdocTeamMember m : members) {
            GdocUser user = userMapper.selectById(m.getUserId());
            if (user != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("username", user.getUsername());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatarUrl());
                map.put("role", m.getRole());
                result.add(map);
            }
        }
        return result;
    }

    @Transactional
    public void delete(Long teamId, Long userId) {
        GdocTeam team = teamMapper.selectById(teamId);
        if (team != null && team.getOwnerId().equals(userId)) {
            teamMemberMapper.delete(new LambdaQueryWrapper<GdocTeamMember>()
                    .eq(GdocTeamMember::getTeamId, teamId));
            teamMapper.deleteById(teamId);
        }
    }

    private Map<String, Object> toMap(GdocTeam team) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", team.getId());
        map.put("name", team.getName());
        map.put("description", team.getDescription());
        map.put("avatarUrl", team.getAvatarUrl());
        map.put("ownerId", team.getOwnerId());
        return map;
    }
}