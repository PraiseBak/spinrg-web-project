package com.mindaces.mindaces.dto;

import com.mindaces.mindaces.domain.entity.Comment;
import com.mindaces.mindaces.domain.entity.Likes;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class CommentDto
{
    Long contentIdx;
    Long boardIdx;
    String gallery;
    String user;
    String content;
    String commentPassword;
    Long isLogged;
    Likes likes;
    LocalDateTime createdDate;
    LocalDateTime modifiedDate;

    public Comment toEntity()
    {
        Comment comment = Comment.builder()
               .contentIdx(contentIdx)
               .boardIdx(boardIdx)
               .gallery(gallery)
               .user(user)
               .content(content)
               .commentPassword(commentPassword)
               .isLogged(isLogged)
               .likes(likes)
               .build();
        return comment;
    }

    @Builder
    public CommentDto(Long contentIdx, Long boardIdx, String gallery, String user, String content, String commentPassword, Long isLogged, Likes likes, LocalDateTime createdDate, LocalDateTime modifiedDate)
    {
        this.contentIdx = contentIdx;
        this.boardIdx = boardIdx;
        this.gallery = gallery;
        this.user = user;
        this.content = content;
        this.commentPassword = commentPassword;
        this.isLogged = isLogged;
        this.likes = likes;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
