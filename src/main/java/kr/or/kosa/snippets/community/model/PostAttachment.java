package kr.or.kosa.snippets.community.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostAttachment {
    private Integer attachmentId;
    private Integer postId;
    private String fileName;
    private String fileType;
    private String filePath;
    private long fileSize;
    private LocalDateTime createdAt;
}
