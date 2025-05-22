package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDraft {
    private Integer draftId;
    private Integer categoryId;
    private String title;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
