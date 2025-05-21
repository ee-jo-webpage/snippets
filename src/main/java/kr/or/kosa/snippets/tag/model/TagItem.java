package kr.or.kosa.snippets.tag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagItem {
    private Long tagId;
    private String name;

    // 매개변수가 있는 생성자
    public TagItem(String name) {
        this.name = name;
    }
}