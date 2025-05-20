package kr.or.kosa.snippets.like.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnippetCode {
    private int snippetId;
    private String content;
    private String language;
}
