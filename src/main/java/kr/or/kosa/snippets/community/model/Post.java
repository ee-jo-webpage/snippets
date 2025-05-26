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
    private boolean isNotice; // primitive boolean (기본값 false)
    private String status; //published, draft, deleted

    //조회용 추가 필드
    private String nickname; //작성자 닉네임
    private String categoryName; //카테고리 이름
    private String commentCount; //댓글 수

    // isNotice getter/setter (boolean용)
    public boolean isNotice() {
        return isNotice;
    }

    public void setNotice(boolean notice) {
        this.isNotice = notice;
    }

    // 또는 Boolean 객체 타입을 사용하려면:
    // private Boolean isNotice; // 이 경우 null 가능
    //
    // public Boolean isNotice() {
    //     return isNotice;
    // }
    //
    // public void setNotice(Boolean notice) {
    //     this.isNotice = notice;
    // }
}