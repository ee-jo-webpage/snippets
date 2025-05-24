package kr.or.kosa.snippets.tag.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagItem {
    private Long tagId;
    private Long userId;  // 사용자 ID 추가
    private String name;

    // 사용자 ID와 태그명으로 생성하는 생성자
    public TagItem(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    // 태그명만으로 생성하는 생성자 (기존 호환성 유지)
    public TagItem(String name) {
        this.name = name;
    }
}