package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Comment {
    private Integer commentId;
    private Integer postId;
    private Long userId;
    private String content;
    private Integer parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //조회용 추가 필드
    private String nickname; //작성자 닉네임
    private Integer likeCount; //좋아요 수
    private Integer dislikeCount; //싫어요 수
    private Boolean isLiked; //현재 사용자의 좋아요 여부
    private Boolean isDisliked; //현재 사용자의 싫어요 여부
    private List<Comment> childComments; //대댓글 목록

}
