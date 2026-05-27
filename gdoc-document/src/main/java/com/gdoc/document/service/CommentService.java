package com.gdoc.document.service;

import com.gdoc.document.mapper.CommentMapper;
import com.gdoc.document.mapper.CommentReplyMapper;
import com.gdoc.model.dto.CommentCreateRequest;
import com.gdoc.model.dto.CommentVO;
import com.gdoc.model.entity.GdocComment;
import com.gdoc.model.entity.GdocCommentReply;
import com.gdoc.user.mapper.UserMapper;
import com.gdoc.model.entity.GdocUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final CommentReplyMapper replyMapper;
    private final UserMapper userMapper;

    public CommentService(CommentMapper commentMapper, CommentReplyMapper replyMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.replyMapper = replyMapper;
        this.userMapper = userMapper;
    }

    public CommentVO create(Long docId, Long userId, CommentCreateRequest request) {
        GdocComment comment = new GdocComment();
        comment.setDocId(docId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setRangeStart(request.getRangeStart());
        comment.setRangeEnd(request.getRangeEnd());
        comment.setResolved(0);
        commentMapper.insert(comment);
        return toVO(comment);
    }

    public List<CommentVO> list(Long docId) {
        List<GdocComment> comments = commentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GdocComment>()
                        .eq(GdocComment::getDocId, docId)
                        .orderByAsc(GdocComment::getCreatedAt));

        Map<Long, String> userNames = comments.stream()
                .map(GdocComment::getUserId)
                .distinct()
                .map(userMapper::selectById)
                .filter(u -> u != null)
                .collect(Collectors.toMap(GdocUser::getId, u -> u.getNickname() != null ? u.getNickname() : u.getUsername()));

        return comments.stream().map(c -> {
            CommentVO vo = toVO(c);
            vo.setUsername(userNames.getOrDefault(c.getUserId(), "未知用户"));
            return vo;
        }).collect(Collectors.toList());
    }

    public void resolve(Long commentId, Integer resolved) {
        GdocComment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setResolved(resolved);
            commentMapper.updateById(comment);
        }
    }

    public void delete(Long commentId) {
        commentMapper.deleteById(commentId);
        // Also delete replies
        replyMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GdocCommentReply>()
                .eq(GdocCommentReply::getCommentId, commentId));
    }

    public CommentVO reply(Long commentId, Long userId, String content) {
        GdocCommentReply reply = new GdocCommentReply();
        reply.setCommentId(commentId);
        reply.setUserId(userId);
        reply.setContent(content);
        replyMapper.insert(reply);
        // Return updated comment
        GdocComment comment = commentMapper.selectById(commentId);
        return toVO(comment);
    }

    private CommentVO toVO(GdocComment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setDocId(c.getDocId());
        vo.setUserId(c.getUserId());
        vo.setContent(c.getContent());
        vo.setRangeStart(c.getRangeStart());
        vo.setRangeEnd(c.getRangeEnd());
        vo.setResolved(c.getResolved());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }
}