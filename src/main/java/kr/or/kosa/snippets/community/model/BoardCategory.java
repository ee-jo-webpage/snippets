package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardCategory {
    private Integer categoryId;
    private String name;
    private String description;
    private Integer orderNum;
    private LocalDateTime createdAt;
}
