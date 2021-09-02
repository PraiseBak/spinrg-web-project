package com.mindaces.mindaces.dto;

import com.mindaces.mindaces.domain.entity.Board;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BoardDto
{
    private Long contentIdx;
    private String gallery;
    private String user;
    private String title;
    private String content;
    private Long likes;
    private Long dislikes;
    private String password;
    private Long isLoggedUser;


    public Board toEntity()
    {
        Board board = Board.builder()
                .gallery(gallery)
                .user(user)
                .contentIdx(contentIdx)
                .title(title)
                .content(content)
                .likes(likes)
                .dislikes(dislikes)
                .password(password)
                .isLoggedUser(isLoggedUser)
                .build();
        return board;
    }


    @Builder
    public BoardDto(String gallery, String user, Long contentIdx, String title, String content,Long likes ,Long dislikes,String password,Long isLoggedUser)
    {
        this.gallery = gallery;
        this.user = user;
        this.contentIdx = contentIdx;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.dislikes = dislikes;
        this.password = password;
        this.isLoggedUser = isLoggedUser;
    }

}
