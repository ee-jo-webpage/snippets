package kr.or.kosa.snippets.snippetExt.model;

import lombok.Data;

@Data
public class SnippetExtUpdate {
    private Long snippetId;
    private Long userId;
    private Long colorId;
    private String memo;
}
