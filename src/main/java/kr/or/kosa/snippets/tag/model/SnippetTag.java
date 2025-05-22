package kr.or.kosa.snippets.tag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnippetTag {
    private Long snippetId;
    private Long tagId;
}
