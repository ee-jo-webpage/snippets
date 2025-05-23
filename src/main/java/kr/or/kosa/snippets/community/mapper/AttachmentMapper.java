package kr.or.kosa.snippets.community.mapper;

import kr.or.kosa.snippets.community.model.PostAttachment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    List<PostAttachment> getAttachmentsByPostId(Integer postId);
    PostAttachment getAttachmentById(Integer attachmentId);
    void insertAttachment(PostAttachment attachment);
    void updateAttachment(PostAttachment attachment);
    void deleteAttachment(Integer attachmentId);
}
