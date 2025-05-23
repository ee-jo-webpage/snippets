package kr.or.kosa.snippets.bookmark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {

    private Long userId;
    private Long snippetId;

}
