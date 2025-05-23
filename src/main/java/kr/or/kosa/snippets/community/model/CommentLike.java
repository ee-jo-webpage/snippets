package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentLike {
    private Integer likeId;
    private Integer commentId;
    private long userId;
    private boolean isLike; // true: 좋아요, false: 싫어요
    private LocalDateTime createdAt;
}
