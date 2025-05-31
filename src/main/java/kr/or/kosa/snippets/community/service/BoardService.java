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
/*
    public void updatePost(Post post, MultipartFile[] files) throws IOException {
        postMapper.updatePost(post);

        if(files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    saveAttachment(post.getPostId(), file);
                }
            }
        }
    }*/

    public void updatePost(Post post, MultipartFile[] files) throws IOException {
        System.out.println("=== BoardService.updatePost 시작 ===");
        System.out.println("업데이트할 Post: " + post.toString());

        try {
            // 기본값 설정
            if (post.getStatus() == null || post.getStatus().isEmpty()) {
                post.setStatus("published");
            }

            // 업데이트 시간 설정 (필요한 경우)
            post.setUpdatedAt(LocalDateTime.now());

            // 실제 업데이트 실행
            int updateCount = postMapper.updatePost(post);
            System.out.println("업데이트된 행 개수: " + updateCount);

            if (updateCount == 0) {
                System.out.println("❌ 업데이트된 행이 없습니다!");
                throw new RuntimeException("게시글 업데이트에 실패했습니다.");
            } else {
                System.out.println("✅ 게시글 업데이트 성공!");
            }

        } catch (Exception e) {
            System.out.println("❌ 게시글 업데이트 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // 파일 처리 (있다면)
        if(files != null && files.length > 0) {
            System.out.println("파일 처리 시작...");
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        saveAttachment(post.getPostId(), file);
                        System.out.println("파일 저장 성공: " + file.getOriginalFilename());
                    } catch (IOException e) {
                        System.out.println("파일 저장 실패: " + file.getOriginalFilename() + " - " + e.getMessage());
                        throw e;
                    }
                }
            }
            System.out.println("파일 처리 완료");
        }

        System.out.println("=== BoardService.updatePost 완료 ===");
    }

    //첨부파일 저장
   /* private void saveAttachment(Integer postId, MultipartFile file) throws IOException {
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

        System.out.println("첨부파일 저장 시도:");
        System.out.println("- 파일명: " + attachment.getFileName());
        System.out.println("- 경로: " + attachment.getFilePath());
        System.out.println("- 크기: " + attachment.getFileSize());

        attachmentMapper.insertAttachment(attachment);
        System.out.println("첨부파일 저장 완료!");
    }*/

    private void saveAttachment(Integer postId, MultipartFile file) throws IOException {
        System.out.println("=== 파일 저장 시작 ===");
        System.out.println("게시글 ID: " + postId);
        System.out.println("원본 파일명: " + file.getOriginalFilename());
        System.out.println("파일 크기: " + file.getSize() + " bytes");
        System.out.println("파일 타입: " + file.getContentType());

        // 업로드 디렉토리 생성
        Path uploadRoot = Paths.get("uploads", "board").toAbsolutePath();
        System.out.println("업로드 디렉토리: " + uploadRoot);

        try {
            Files.createDirectories(uploadRoot);
            System.out.println("✅ 디렉토리 생성/확인 완료");
        } catch (IOException e) {
            System.out.println("❌ 디렉토리 생성 실패: " + e.getMessage());
            throw e;
        }

        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = uploadRoot.resolve(storedFilename);

        System.out.println("저장될 파일명: " + storedFilename);
        System.out.println("저장될 경로: " + targetPath);

        try {
            // 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ 파일 저장 완료");

            // 저장된 파일 확인
            if (Files.exists(targetPath)) {
                System.out.println("✅ 파일 존재 확인 완료");
                System.out.println("저장된 파일 크기: " + Files.size(targetPath) + " bytes");
            } else {
                System.out.println("❌ 파일 저장 후 존재하지 않음!");
            }

        } catch (IOException e) {
            System.out.println("❌ 파일 저장 실패: " + e.getMessage());
            throw e;
        }

        // DB에 저장할 정보 준비
        PostAttachment attachment = new PostAttachment();
        attachment.setPostId(postId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(targetPath.toString()); // 절대 경로로 저장
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());

        System.out.println("DB 저장 정보:");
        System.out.println("- 파일명: " + attachment.getFileName());
        System.out.println("- 파일 경로: " + attachment.getFilePath());
        System.out.println("- 파일 크기: " + attachment.getFileSize());

        try {
            attachmentMapper.insertAttachment(attachment);
            System.out.println("✅ DB 저장 완료 - Attachment ID: " + attachment.getAttachmentId());
        } catch (Exception e) {
            System.out.println("❌ DB 저장 실패: " + e.getMessage());
            // 파일 삭제 (롤백)
            try {
                Files.deleteIfExists(targetPath);
                System.out.println("파일 롤백 완료");
            } catch (IOException deleteE) {
                System.out.println("파일 롤백 실패: " + deleteE.getMessage());
            }
            throw e;
        }

        System.out.println("=== 파일 저장 완료 ===");
    }


    //검색 기능
    public List<Post> searchPosts(String keyword, String searchType, Integer categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        return postMapper.searchPosts(keyword, searchType, categoryId, startDate, endDate);
    }

    //임시저장 관련 메서드
    public List<PostDraft> getDraftsByUserId(Long userId){
        return postMapper.getDraftsByUserId(userId);
    }


    /**
     * 게시글의 첨부파일 목록 조회
     */
    public List<PostAttachment> getAttachmentsByPostId(Integer postId) {
        return attachmentMapper.getAttachmentsByPostId(postId);
    }

    /**
     * 첨부파일 상세 조회
     */
    public PostAttachment getAttachmentById(Integer attachmentId) {
        return attachmentMapper.getAttachmentById(attachmentId);
    }

    /**
     * 첨부파일 삭제
     */
    public void deleteAttachment(Integer attachmentId) {
        attachmentMapper.deleteAttachment(attachmentId);
    }

    /**
     * 임시저장 상세 조회
     */
    public PostDraft getDraftById(Integer draftId) {
        return postMapper.getDraftById(draftId);
    }

    /**
     * 임시저장
     */
    public Integer saveDraft(PostDraft draft) {
        if (draft.getDraftId() == null) {
            // 새로 생성
            postMapper.insertDraft(draft);
        } else {
            // 기존 것 업데이트
            postMapper.updateDraft(draft);
        }
        return draft.getDraftId();
    }

    /**
     * 임시저장 삭제
     */
    public void deleteDraft(Integer draftId) {
        postMapper.deleteDraft(draftId);
    }

    public void deletePost(Integer postId) {
        System.out.println("=== BoardService.deletePost 시작 ===");
        System.out.println("삭제할 게시글 ID: " + postId);

        try {
            // 1. 먼저 첨부파일들을 조회하여 실제 파일 삭제
            List<PostAttachment> attachments = attachmentMapper.getAttachmentsByPostId(postId);

            if (attachments != null && !attachments.isEmpty()) {
                System.out.println("첨부파일 " + attachments.size() + "개 삭제 시작");

                for (PostAttachment attachment : attachments) {
                    try {
                        // 실제 파일 삭제
                        Path filePath = Paths.get(attachment.getFilePath());
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            System.out.println("파일 삭제 성공: " + attachment.getFileName());
                        }

                        // DB에서 첨부파일 정보 삭제
                        attachmentMapper.deleteAttachment(attachment.getAttachmentId());

                    } catch (IOException e) {
                        System.out.println("파일 삭제 실패: " + attachment.getFileName() + " - " + e.getMessage());
                        // 파일 삭제 실패해도 계속 진행 (DB 정보는 삭제)
                    }
                }
            }

            // 2. 댓글들도 삭제 (CASCADE 설정이 안되어 있다면)
            // 실제로는 데이터베이스에서 CASCADE DELETE로 처리하는 것이 좋습니다
            // 여기서는 우선 게시글만 삭제

            // 3. 게시글 삭제
            postMapper.deletePost(postId);
            System.out.println("✅ 게시글 삭제 완료");

        } catch (Exception e) {
            System.out.println("❌ 게시글 삭제 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("게시글 삭제 중 오류가 발생했습니다.", e);
        }

        System.out.println("=== BoardService.deletePost 완료 ===");
    }





}
