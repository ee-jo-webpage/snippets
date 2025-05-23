package kr.or.kosa.snippets.community.service;

import kr.or.kosa.snippets.community.mapper.*;
import kr.or.kosa.snippets.community.model.BoardCategory;
import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.model.PostAttachment;
import kr.or.kosa.snippets.community.model.PostDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final BoardCategoryMapper categoryMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final AttachmentMapper attachmentMapper;
    private final NotificationMapper notificationMapper;

    //카테고리 관련 메서드
    public List<BoardCategory> getAllCategories() {
        return categoryMapper.getAllCategories();
    }

    public BoardCategory getCategoryById(Integer categoryId) {
        return categoryMapper.getCategoryById(categoryId);
    }

    //게시글 관련 메서드
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    public List<Post> getPostsByCategoryId(Integer categoryId) {
        return postMapper.getPostsByCategoryId(categoryId);
    }

    public Post getPostById(Integer postId) {
        Post post = postMapper.getPostById(postId);
        if (post != null) {
            postMapper.increaseViewCount(postId);
        }
        return post;
    }

    public Integer createPost(Post post, MultipartFile[] files) throws IOException {
        post.setStatus("published");
        postMapper.insertPost(post);

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    saveAttachment(post.getPostId(), file);
                }
            }
        }

        return post.getPostId();
    }

    public void updatePost(Post post, MultipartFile[] files) throws IOException {
        postMapper.updatePost(post);

        if(files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    saveAttachment(post.getPostId(), file);
                }
            }
        }
    }

    public void deletePost(Integer postId) {
        postMapper.deletePost(postId);
    }

    //첨부파일 저장
    private void saveAttachment(Integer postId, MultipartFile file) throws IOException {
        Path uploadRoot = Paths.get("uploads","board").toAbsolutePath();
        Files.createDirectories(uploadRoot);

        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = uploadRoot.resolve(storedFilename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        PostAttachment attachment = new PostAttachment();
        attachment.setPostId(postId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath("/uploads/board/" + storedFilename);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());

        attachmentMapper.insertAttachment(attachment);

    }

    //검색 기능
    public List<Post> searchPosts(String keyword, String searchType, Integer categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        return postMapper.searchPosts(keyword, searchType, categoryId, startDate, endDate);
    }

    //임시저장 관련 메서드
    public List<PostDraft> getDraftsByUserId(Long userId){
        return postMapper.getDraftsByUserId(userId);
    }










}
