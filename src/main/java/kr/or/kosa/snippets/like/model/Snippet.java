package kr.or.kosa.snippets.like.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snippet {
    private Long snippetId;      // snippet_id
    private Long userId;         // user_id
    private Long colorId;        // color_id
    private String sourceUrl;   // source_url
    private String type;        // type
    private String memo;        // memo
    private LocalDateTime createdAt;  // created_at
    private LocalDateTime updatedAt;  // updated_at
    private int likeCount;      // like_count
    private boolean visibility; // visibility
}