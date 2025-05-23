package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Integer postId;
    private Integer userId;
    private Integer categoryId;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isNotice;
    private String status; //published, draft, deleted

    //조회용 추가 필드
    private String nickname; //작성자 닉네임
    private String categoryName; //카테고리 이름
    private String commentCount; //댓글 수

}
