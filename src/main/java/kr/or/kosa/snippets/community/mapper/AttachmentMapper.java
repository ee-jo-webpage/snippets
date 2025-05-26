package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.PostAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    /**
     * 게시글의 첨부파일 목록 조회
     */
    List<PostAttachment> getAttachmentsByPostId(@Param("postId") Integer postId);

    /**
     * 첨부파일 상세 조회
     */
    PostAttachment getAttachmentById(@Param("attachmentId") Integer attachmentId);

    /**
     * 첨부파일 등록
     */
    void insertAttachment(PostAttachment attachment);

    /**
     * 첨부파일 삭제
     */
    void deleteAttachment(@Param("attachmentId") Integer attachmentId);
}