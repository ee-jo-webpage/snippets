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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final NotificationService notificationService;

    // 게시판 목록
    @GetMapping
    public String boardList(Model model) {
        try {
            List<BoardCategory> categories = boardService.getAllCategories();
            model.addAttribute("categories", categories);
            return "community/categoryList";
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

            return "community/allPostsList";
        } catch (Exception e) {
            model.addAttribute("error", "게시글 목록을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }
    // 게시글 목록
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
            Post post = boardService.getPostById(postId);
            if (post == null) {
                return "redirect:/community";
            }

            // CustomUserDetails에서 사용자 ID 추출
            Long currentUserId = getCurrentUserId(userDetails);
            List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

            model.addAttribute("post", post);
            model.addAttribute("attachments", attachments);
            model.addAttribute("currentUserId", currentUserId);
            model.addAttribute("isLoggedIn", userDetails != null);
            model.addAttribute("currentUserNickname", userDetails != null ? userDetails.getNickname() : null);

            return "community/postDetail";
        } catch (Exception e) {
            model.addAttribute("error", "게시글을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // 게시글 작성 폼
    @GetMapping("/post/new")
    public String newPostForm(Model model,
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

            return "community/postForm";
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "게시글 작성 폼을 불러올 수 없습니다.");
            return "redirect:/community";
        }
    }

    // BoardController.java - addPost 메서드 수정
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

            // 사용자 ID 설정 - ✅ .intValue() 제거
            if (userId != null) {
                post.setUserId(userId);  // Long → Long 직접 설정
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

            // 공지사항 체크박스 처리 (체크되지 않으면 false로 설정)
            System.out.println("공지사항 설정 전: " + post.isNotice());
            // boolean primitive 타입이므로 별도 처리 불필요
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
            // 작성자 또는 관리자만 수정 가능 - ✅ .intValue() 제거
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
    /*
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/login?message=loginRequired";
        }

        try {
            Long currentUserId = getCurrentUserId(userDetails);
            Post existingPost = boardService.getPostById(postId);

            // 권한 체크 - ✅ .intValue() 제거
            if (existingPost == null ||
                    (!existingPost.getUserId().equals(currentUserId) && !isAdmin(userDetails))) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            post.setPostId(postId);
            post.setUserId(currentUserId);
            boardService.updatePost(post, files);
            return "redirect:/community/post/" + postId + "?success=updated";

        } catch (IOException e) {
            return "redirect:/community/post/" + postId + "/edit?error=file";
        } catch (Exception e) {
            return "redirect:/community/post/" + postId + "/edit?error=save";
        }
    }
    */

    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {


        // 여기서 userId 설정하는 부분 확인
        Long currentUserId = getCurrentUserId(userDetails);

        // 이 부분이 제대로 실행되고 있는지 확인
        post.setUserId(currentUserId);

        System.out.println("userId 설정 후: " + post.getUserId()); // 이 로그 추가


        System.out.println("=== 게시글 수정 디버깅 ===");
        System.out.println("Post ID: " + postId);
        System.out.println("제목: " + post.getTitle());
        System.out.println("내용: " + post.getContent());
        System.out.println("카테고리 ID: " + post.getCategoryId());
        System.out.println("공지사항: " + post.isNotice());

        // 추가 디버깅: 모든 파라미터 확인
        System.out.println("=== Post 객체 전체 ===");
        System.out.println("Post 객체: " + post.toString());

        try {
            currentUserId = getCurrentUserId(userDetails);
            Post existingPost = boardService.getPostById(postId);
            System.out.println("기존 게시글 조회 완료: " + existingPost);

            if (existingPost == null) {
                System.out.println("❌ 게시글이 존재하지 않음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            Long existingUserId = existingPost.getUserId(); // Long 타입으로 받기
            Long currentUserIdLong = currentUserId;         // Long 그대로 사용

            System.out.println("권한 체크: 기존 userId=" + existingUserId + ", 현재 userId=" + currentUserIdLong);
            System.out.println("userId 같은가? " + existingUserId.equals(currentUserIdLong));

            boolean isOwner = existingUserId.equals(currentUserIdLong);
            boolean isAdminUser = isAdmin(userDetails);

            System.out.println("소유자인가? " + isOwner);
            System.out.println("관리자인가? " + isAdminUser);

            if (!isOwner && !isAdminUser) {
                System.out.println("❌ 권한 없음");
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            System.out.println("✅ 권한 체크 통과");
            System.out.println("boardService.updatePost() 호출 직전");

            boardService.updatePost(post, files);
            System.out.println("boardService.updatePost() 호출 완료");

            System.out.println("수정 완료!");
            return "redirect:/community/post/" + postId + "?success=updated";

        } catch (Exception e) {
            System.out.println("수정 오류: " + e.getMessage());
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
            // 권한 체크 - ✅ .intValue() 제거
            if (!post.getUserId().equals(currentUserId) && !isAdmin(userDetails)) {
                return "redirect:/community/post/" + postId + "?error=permission";
            }

            Integer categoryId = post.getCategoryId();
            boardService.deletePost(postId);
            return "redirect:/community/category/" + categoryId + "?success=deleted";

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
        try {
            PostAttachment attachment = boardService.getAttachmentById(attachmentId);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
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