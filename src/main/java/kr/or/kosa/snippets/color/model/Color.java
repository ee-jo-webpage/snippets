package kr.or.kosa.snippets.color.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    private Long userId;
    private Long colorId;
    private String name;
    private String hexCode;
}
