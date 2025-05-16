package kr.or.kosa.snippets.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnippetImage {
    private int snippetId;
    private String imageUrl;
    private String altText;
}