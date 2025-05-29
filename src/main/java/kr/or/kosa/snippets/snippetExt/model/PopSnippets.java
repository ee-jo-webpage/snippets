package kr.or.kosa.snippets.snippetExt.model;

import kr.or.kosa.snippets.snippetExt.type.SnippetType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PopSnippets {
    private Long snippetId;
    private Long userId;
    private Long folderId;
    private Long colorId;
    private String sourceUrl;
    private SnippetType type;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean visibility;
    private Long likeCount;
    private String language;
    private String content;
    private String imageUrl;

    public String getTypeName() {
        return type != null ? type.name() : null;
    }
}
