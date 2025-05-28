package kr.or.kosa.snippets.snippetExt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.or.kosa.snippets.snippetExt.type.SnippetType;
import lombok.Data;

@Data
public class SnippetExtCreate {

    private Long snippetId; // 생성 시 필요 없음

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    private Long colorId;

    @NotBlank(message = "출처 URL은 필수입니다.")
    private String sourceUrl;

    @NotNull(message = "스니펫 타입은 필수입니다.")
    private SnippetType type;

    private String memo;

    // TEXT, CODE 공통
    private String content;

    // CODE 전용
    private String language;

    // IMG 전용
    private String imageUrl;
    private String altText;

    // uuid
    private String clientRequestId;

    public String getTypeName() {
        return type != null ? type.name() : null;
    }

}
