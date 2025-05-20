package kr.or.kosa.snippets.snippetExt.model;

import kr.or.kosa.snippets.snippetExt.type.SnippetType;
import lombok.Data;

@Data
public class SnippetExtCreate {
    private Long snippetId;
    private Long colorId;
    private String sourceUrl;
    private SnippetType type;
    private String memo;
    private String content; // text, code
    private String language;
    private String imageUrl;
    private String altText;
}