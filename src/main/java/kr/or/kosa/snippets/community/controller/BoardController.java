package kr.or.kosa.snippets.community.controller;

import kr.or.kosa.snippets.community.model.BoardCategory;
import kr.or.kosa.snippets.community.model.Post;
import kr.or.kosa.snippets.community.model.PostAttachment;
import kr.or.kosa.snippets.community.model.PostDraft;
import kr.or.kosa.snippets.community.service.BoardService;
import kr.or.kosa.snippets.community.service.CommentService;
import kr.or.kosa.snippets.community.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

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
        List<BoardCategory> categories = boardService.getAllCategories();
        model.addAttribute("categories", categories);
        return "community/categoryList";
    }

    // 게시글 목록
    @GetMapping("/category/{categoryId}")
    public String postList(@PathVariable Integer categoryId, Model model) {
        BoardCategory category = boardService.getCategoryById(categoryId);
        List<Post> posts = boardService.getPostsByCategoryId(categoryId);

        model.addAttribute("category", category);
        model.addAttribute("posts", posts);
        return "community/postList";
    }

    // 게시글 상세
    @GetMapping("/post/{postId}")
    public String postDetail(@PathVariable Integer postId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Post post = boardService.getPostById(postId);
        if (post == null) {
            return "redirect:/community";
        }

        Long currentUserId = null;
        if (userDetails != null) {
            // UserDetails에서 사용자 ID 추출
            // 실제 구현에서는 UserDetails에서 ID를 가져오는 방식에 맞게 수정 필요
            // currentUserId = ((CustomUserDetails) userDetails).getUserId();
        }

        List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

        model.addAttribute("post", post);
        model.addAttribute("attachments", attachments);
        model.addAttribute("currentUserId", currentUserId);

        return "community/postDetail";
    }

    // 게시글 작성 폼
    @GetMapping("/post/new")
    public String newPostForm(Model model) {
        List<BoardCategory> categories = boardService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("post", new Post());
        return "community/postForm";
    }

    // 게시글 작성 처리
    @PostMapping("/post/new")
    public String addPost(@ModelAttribute Post post,
                          @RequestParam(value = "files", required = false) MultipartFile[] files,
                          @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        // 사용자 ID 설정
        // post.setUserId(((CustomUserDetails) userDetails).getUserId());

        Integer postId = boardService.createPost(post, files);
        return "redirect:/community/post/" + postId;
    }

    // 게시글 수정 폼
    @GetMapping("/post/{postId}/edit")
    public String editPostForm(@PathVariable Integer postId, Model model) {
        Post post = boardService.getPostById(postId);
        if (post == null) {
            return "redirect:/community";
        }

        List<BoardCategory> categories = boardService.getAllCategories();
        List<PostAttachment> attachments = boardService.getAttachmentsByPostId(postId);

        model.addAttribute("post", post);
        model.addAttribute("categories", categories);
        model.addAttribute("attachments", attachments);

        return "community/postEditForm";
    }

    // 게시글 수정 처리
    @PostMapping("/post/{postId}/edit")
    public String updatePost(@PathVariable Integer postId,
                             @ModelAttribute Post post,
                             @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {
        post.setPostId(postId);
        boardService.updatePost(post, files);
        return "redirect:/community/post/" + postId;
    }

    // 게시글 삭제
    @PostMapping("/post/{postId}/delete")
    public String deletePost(@PathVariable Integer postId) {
        Post post = boardService.getPostById(postId);
        if (post == null) {
            return "redirect:/community";
        }

        boardService.deletePost(postId);
        return "redirect:/community/category/" + post.getCategoryId();
    }

    // 게시글 검색
    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String searchType,
                              @RequestParam(required = false) Integer categoryId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                              Model model) {

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
    }

    // 첨부파일 다운로드
    @GetMapping("/attachment/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Integer attachmentId) {
        PostAttachment attachment = boardService.getAttachmentById(attachmentId);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}