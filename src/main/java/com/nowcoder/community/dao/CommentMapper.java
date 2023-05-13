package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 分页查询
     * @param entityType 评论类型
     * @param entityId 评论的具体内容的id
     * @param offset 分页查询的条件
     * @param limit 每页显示的行数的限制
     * @return
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
