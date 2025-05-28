package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.BoardCategory;
import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.model.PostAttachment;
import kr.or.kosa.snippets.community.service.BoardService;
import kr.or.kosa.snippets.community.service.CommentService;
import kr.or.kosa.snippets.community.service.NotificationService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final NotificationService notificationService;

    // 커뮤니티 메인 페이지 - 자유게시판을 기본으로 설정
    @GetMapping
    public String boardMain(@RequestParam(required = false) Integer categoryId, Model model) {
        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            List<Post> posts;
            Integer currentCategoryId;
            String currentCategoryName;

            if (categoryId != null) {
                // 특정 카테고리가 지정된 경우
                posts = boardService.getPostsByCategoryId(categoryId);
                BoardCategory category = boardService.getCategoryById(categoryId);
                currentCategoryId = categoryId;
                currentCategoryName = category != null ? category.getName() : "알 수 없음";
            } else {
                // 기본값: 자유게시판 (categoryId = 1)
                currentCategoryId = 1;
                posts = boardService.getPostsByCategoryId(1);
                BoardCategory freeBoard = boardService.getCategoryById(1);
                currentCategoryName = freeBoard != null ? freeBoard.getName() : "자유게시판";
            }

            model.addAttribute("categories", categories);
            model.addAttribute("posts", posts);
            model.addAttribute("currentCategoryId", currentCategoryId);
            model.addAttribute("currentCategoryName", currentCategoryName);

            return "community/main"; // 새로운 메인 템플릿
        } catch (Exception e) {
            model.addAttribute("error", "데이터베이스 연결에 문제가 있습니다. 관리자에게 문의하세요.");
            return "error/database-error";
        }
    }

    // 전체 게시글 목록
    @GetMapping("/posts/all")
    public String allPostsList(Model model) {
        try {
            List<Post> allPosts = boardService.getAllPosts();
            List<BoardCategory> categories = boardService.getAllCategories();

            model.addAttribute("posts", allPosts);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "전체 게시글");
            model.addAttribute("showAllPosts", true);
            model.addAttribute("currentCategoryId", "all");
            model.addAttribute("currentCategoryName", "전체 게시글");

            return "community/main"; // 동일한 템플릿 사용
        } catch (Exception e) {
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // API: 카테고리별 게시글 목록 (AJAX용)
    @GetMapping("/api/posts")
    @ResponseBody
    public Map<String, Object> getPostsByCategory(@RequestParam(required = false) Integer categoryId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts;

            if (categoryId != null) {
                posts = boardService.getPostsByCategoryId(categoryId);
            } else {
                posts = boardService.getAllPosts();
            }

            response.put("success", true);
            response.put("posts", posts);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "게시글을 불러올 수 없습니다.");
        }

        return response;
    }

    // API: 전체 게시글 목록 (AJAX용)
    @GetMapping("/api/posts/all")
    @ResponseBody
    public Map<String, Object> getAllPosts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts = boardService.getAllPosts();
            response.put("success", true);
            response.put("posts", posts);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "게시글을 불러올 수 없습니다.");
        }

        return response;
    }

    // 기존 카테고리별 게시글 목록 (하위 호환성을 위해 유지)
    @GetMapping("/category/{categoryId}")
    public String postList(@PathVariable Integer categoryId, Model model) {
        try {
            BoardCategory category = boardService.getCategoryById(categoryId);
            List<Post> posts = boardService.getPostsByCategoryId(categoryId);

            model.addAttribute("category", category);
            model.addAttribute("posts", posts);
            return "community/postList";
        } catch (Exception e) {
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // 게시글 상세
    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        try {
            System.out.println("=== 게시글 상세 조회 시작 ===");
            System.out.println("요청된 게시글 ID: " + postId);

            Post post = boardService.getPostById(postId);
            if (post == null) {
                System.out.println("❌ 게시글을 찾을 수 없음: " + postId);
                return "redirect:/community";
            }

            System.out.println("✅ 게시글 조회 성공: " + post.getTitle());

            // 현재 사용자 정보
            Long currentUserId = null;
            boolean isLoggedIn = false;
            String currentUserNickname = null;

            if (userDetails != null) {
                currentUserId = userDetails.getUserId();
                isLoggedIn = true;
                currentUserNickname = userDetails.getNickname();
                System.out.println("현재 사용자 ID: " + currentUserId);
            } else {
                System.out.println("비로그인 사용자");
            }

            // 첨부파일 조회 (안전하게 처리)
            List<PostAttachment> attachments = null;
            try {
                attachments = boardService.getAttachmentsByPostId(postId);
                System.out.println("첨부파일 개수: " + (attachments != null ? attachments.size() : 0));
            } catch (Exception e) {
                System.out.println("첨부파일 조회 실패: " + e.getMessage());
                attachments = new ArrayList<>(); // 빈 리스트로 초기화
            }

            // 모델에 데이터 추가
            model.addAttribute("post", post);
            model.addAttribute("attachments", attachments != null ? attachments : new ArrayList<>());
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("isLoggedIn", isLoggedIn);
            model.addAttribute("currentUserNickname", currentUserNickname);

            System.out.println("=== 모델 데이터 설정 완료 ===");
            return "community/postDetail";

        } catch (Exception e) {
            System.out.println("❌ 게시글 상세 조회 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "게시글을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // 게시글 작성 폼
    @GetMapping("/post/new")
    public String newPostForm(@RequestParam(required = false) Integer categoryId,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 디버깅용 로그 추가
        System.out.println("=== 새 게시글 폼 접근 ===");
        if (userDetails != null) {
            System.out.println("현재 사용자 ID: " + userDetails.getUserId());
            System.out.println("현재 사용자 타입: " + userDetails.getUserId().getClass().getSimpleName());
            System.out.println("현재 사용자 닉네임: " + userDetails.getNickname());
            System.out.println("현재 사용자 이메일: " + userDetails.getUsername());
        } else {
            System.out.println("로그인되지 않음");
            return "redirect:/login?message=loginRequired";
        }

        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            System.out.println("카테고리 개수: " + categories.size());

            model.addAttribute("categories", categories);
            model.addAttribute("post", new Post());
            model.addAttribute("currentUserNickname", userDetails.getNickname());

            // 선택된 카테고리가 있으면 기본값으로 설정
            if (categoryId != null) {
                model.addAttribute("selectedCategoryId", categoryId);
            }

            return "community/postForm";
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "게시글 작성 폼을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // 게시글 등록 처리
    @PostMapping("/post/new")
    public String addPost(@ModelAttribute Post post,
                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {

        System.out.println("=== 게시글 등록 디버깅 시작 ===");

        // 로그인 체크
        if (userDetails == null) {
            System.out.println("ERROR: 사용자가 로그인되지 않음");
            return "redirect:/login?message=loginRequired";
        }

        try {
            // 사용자 정보 로깅
            Long userId = getCurrentUserId(userDetails);
            System.out.println("현재 사용자 ID: " + userId);
            System.out.println("현재 사용자 닉네임: " + userDetails.getNickname());

            // 게시글 정보 로깅
            System.out.println("게시글 제목: " + post.getTitle());
            System.out.println("게시글 내용 길이: " + (post.getContent() != null ? post.getContent().length() : 0));
            System.out.println("카테고리 ID: " + post.getCategoryId());
            System.out.println("공지사항 여부: " + post.isNotice());

            // 필수 필드 검증
            if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
                System.out.println("ERROR: 제목이 비어있음");
                return "redirect:/community/post/new?error=title";
            }

            if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                System.out.println("ERROR: 내용이 비어있음");
                return "redirect:/community/post/new?error=content";
            }

            if (post.getCategoryId() == null) {
                System.out.println("ERROR: 카테고리가 선택되지 않음");
                return "redirect:/community/post/new?error=category";
            }

            // 사용자 ID 설정
            if (userId != null) {
                post.setUserId(userId.intValue());
                System.out.println("Post에 설정된 사용자 ID: " + post.getUserId());
            } else {
                System.out.println("ERROR: 사용자 ID가 null");
                return "redirect:/community/post/new?error=user";
            }

            // 기본값 설정
            if (post.getStatus() == null || post.getStatus().isEmpty()) {
                post.setStatus("published");
                System.out.println("게시글 상태를 'published'로 설정");
            }

            // 공지사항 체크박스 처리
            System.out.println("공지사항 설정 전: " + post.isNotice());
            System.out.println("공지사항 설정 후: " + post.isNotice());

            // 파일 정보 로깅
            if (files != null) {
                System.out.println("첨부파일 개수: " + files.length);
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isEmpty()) {
                        System.out.println("파일 " + (i+1) + ": " + files[i].getOriginalFilename()
                                + " (크기: " + files[i].getSize() + " bytes)");
                    }
                }
            }

            // 게시글 생성
            System.out.println("게시글 생성 시작...");
            Integer postId = boardService.createPost(post, files);
            System.out.println("게시글 생성 완료. 생성된 게시글 ID: " + postId);

            System.out.println("=== 게시글 등록 성공 ===");
            return "redirect:/community/post/" + postId + "?success=created";

        } catch (IOException e) {
            System.out.println("ERROR: 파일 업로드 오류 - " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/new?error=file";
        } catch (Exception e) {
            System.out.println("ERROR: 게시글 저장 오류 - " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/new?error=save";
        }
    }

    // 게시글 수정 폼
    @GetMapping("/post/{postId}/edit")
    public String editPostForm(@PathVariable Integer postId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            // 작성자 또는 관리자만 수정 가능
            if (!post.getUserId().equals(currentUserId) && !isAdmin(userDetails)) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            List<BoardCategory> categories = boardService.getAllCategories();
            List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

            model.addAttribute("post", post);
            model.addAttribute("categories", categories);
            model.addAttribute("attachments", attachments);

            return "community/postEdit";
        } catch (Exception e) {
            return "redirect:/community";
        }
    }

    // 게시글 수정 처리
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        System.out.println("=== 게시글 수정 디버깅 ===");
        System.out.println("Post ID: " + postId);
        System.out.println("제목: " + post.getTitle());
        System.out.println("내용: " + post.getContent());
        System.out.println("카테고리 ID: " + post.getCategoryId());
        System.out.println("공지사항: " + post.isNotice());

        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Long currentUserId = getCurrentUserId(userDetails);
            Post existingPost = boardService.getPostById(postId);

            if (existingPost == null) {
                System.out.println("❌ 게시글이 존재하지 않음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            // 권한 체크
            boolean isOwner = existingPost.getUserId().equals(currentUserId);
            boolean isAdminUser = isAdmin(userDetails);

            System.out.println("권한 체크: 기존 userId=" + existingPost.getUserId() + ", 현재 userId=" + currentUserId);
            System.out.println("소유자인가? " + isOwner);
            System.out.println("관리자인가? " + isAdminUser);

            if (!isOwner && !isAdminUser) {
                System.out.println("❌ 권한 없음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            // 필수 정보 설정
            post.setPostId(postId);
            post.setUserId(currentUserId.intValue());

            System.out.println("✅ 권한 체크 통과, 업데이트 시작");

            boardService.updatePost(post, files);

            System.out.println("✅ 수정 완료!");
            return "redirect:/community/post/" + postId + "?success=updated";

        } catch (Exception e) {
            System.out.println("❌ 수정 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/community/post/" + postId + "/edit?error=save";
        }
    }

    // 게시글 삭제
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Integer postId,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            Long currentUserId = getCurrentUserId(userDetails);
            // 권한 체크
            if (!post.getUserId().equals(currentUserId) && !isAdmin(userDetails)) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            Integer categoryId = post.getCategoryId();
            boardService.deletePost(postId);

            // 삭제 후 해당 카테고리로 리다이렉트 (메인 페이지 형태)
            return "redirect:/community?categoryId=" + categoryId + "&success=deleted";

        } catch (Exception e) {
            return "redirect:/community";
        }
    }

    // 게시글 검색
    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String searchType,
                              @RequestParam(required = false) Integer categoryId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                              Model model) {
        try {
            List<Post> posts = boardService.searchPosts(keyword, searchType, categoryId, startDate, endDate);
            List<BoardCategory> categories = boardService.getAllCategories();

            model.addAttribute("posts", posts);
            model.addAttribute("categories", categories);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchType", searchType);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);

            return "community/searchResults";
        } catch (Exception e) {
            return "redirect:/community";
        }
    }

    // 첨부파일 다운로드
    @GetMapping("/attachment/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Integer attachmentId) {
        System.out.println("=== 첨부파일 다운로드 요청 ===");
        System.out.println("요청된 Attachment ID: " + attachmentId);

        try {
            // 1단계: 서비스 메서드 호출 전 로그
            System.out.println("BoardService.getAttachmentById() 호출 시작...");

            PostAttachment attachment = boardService.getAttachmentById(attachmentId);

            // 2단계: 서비스 메서드 호출 후 결과 확인
            System.out.println("BoardService.getAttachmentById() 호출 완료");

            if (attachment == null) {
                System.out.println("❌ 첨부파일을 찾을 수 없음 - ID: " + attachmentId);
                return ResponseEntity.notFound().build();
            }

            // 3단계: 첨부파일 정보 출력
            System.out.println("✅ 첨부파일 조회 성공:");
            System.out.println("- ID: " + attachment.getAttachmentId());
            System.out.println("- 파일명: " + attachment.getFileName());
            System.out.println("- 파일 경로: " + attachment.getFilePath());
            System.out.println("- 파일 타입: " + attachment.getFileType());
            System.out.println("- 파일 크기: " + attachment.getFileSize());
            System.out.println("- 게시글 ID: " + attachment.getPostId());

            // 4단계: 파일 경로 처리
            Path filePath;

            // 상대 경로인지 절대 경로인지 확인
            if (attachment.getFilePath().startsWith("/uploads/")) {
                // 상대 경로인 경우 절대 경로로 변환
                String fileName = attachment.getFilePath().substring(attachment.getFilePath().lastIndexOf("/") + 1);
                filePath = Paths.get("uploads", "board", fileName).toAbsolutePath();
                System.out.println("- 상대 경로에서 절대 경로로 변환");
            } else {
                // 이미 절대 경로인 경우
                filePath = Paths.get(attachment.getFilePath());
                System.out.println("- 절대 경로 사용");
            }

            System.out.println("- 최종 파일 경로: " + filePath.toAbsolutePath());
            System.out.println("- 파일 존재 여부: " + Files.exists(filePath));

            Resource resource = new UrlResource(filePath.toUri());

            // 5단계: 파일 존재 및 읽기 가능 여부 확인
            System.out.println("- 파일 존재 여부: " + resource.exists());
            System.out.println("- 파일 읽기 가능: " + resource.isReadable());

            if (resource.exists() && resource.isReadable()) {
                System.out.println("✅ 파일 다운로드 시작");

                // 한글 파일명 인코딩 처리
                String encodedFileName;
                try {
                    encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8.toString())
                            .replaceAll("\\+", "%20");
                } catch (Exception e) {
                    encodedFileName = "download_file";
                }

                System.out.println("- 원본 파일명: " + attachment.getFileName());
                System.out.println("- 인코딩된 파일명: " + encodedFileName);

                return ResponseEntity.ok()
                        // 강제 다운로드를 위한 헤더 설정
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getFileName() + "\"; filename*=UTF-8''" + encodedFileName)
                        .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream") // 강제 다운로드
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(resource);
            } else {
                System.out.println("❌ 파일이 존재하지 않거나 읽을 수 없음");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.out.println("❌ 첨부파일 다운로드 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 이미지 업로드 (Summernote용)
    @PostMapping("/upload-image")
    @ResponseBody
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, String> result = new HashMap<>();

        // 로그인 체크
        if (userDetails == null) {
            result.put("error", "로그인이 필요합니다.");
            return result;
        }

        try {
            if (file.isEmpty()) {
                result.put("error", "파일이 비어있습니다.");
                return result;
            }

            // 이미지 파일 검증
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.put("error", "이미지 파일만 업로드 가능합니다.");
                return result;
            }

            // 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("error", "파일 크기는 5MB를 초과할 수 없습니다.");
                return result;
            }

            // 업로드 디렉토리 생성
            Path uploadDir = Paths.get("uploads", "images").toAbsolutePath();
            Files.createDirectories(uploadDir);

            // 파일명 생성 (중복 방지)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 웹에서 접근 가능한 URL 반환
            result.put("imageUrl", "/uploads/images/" + filename);

        } catch (IOException e) {
            result.put("error", "파일 업로드 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * 현재 로그인된 사용자 정보 확인용 (디버깅용)
     */
    @GetMapping("/debug/user")
    @ResponseBody
    public Map<String, Object> getCurrentUserDebug(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();

        if (userDetails != null) {
            result.put("success", true);
            result.put("userId", userDetails.getUserId());
            result.put("nickname", userDetails.getNickname());
            result.put("email", userDetails.getUsername()); // 이메일
            result.put("authorities", userDetails.getAuthorities().toString());
            result.put("userIdType", userDetails.getUserId().getClass().getSimpleName());
        } else {
            result.put("success", false);
            result.put("error", "로그인되지 않음");
            result.put("message", "로그인이 필요합니다.");
        }

        return result;
    }

    /**
     * CustomUserDetails에서 사용자 ID 추출
     */
    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userDetails.getUserId();
    }

    /**
     * 관리자 권한 체크
     */
    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}