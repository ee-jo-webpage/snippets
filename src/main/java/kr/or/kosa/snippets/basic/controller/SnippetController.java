package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.S3Service;
import kr.or.kosa.snippets.basic.service.SnippetService;
import kr.or.kosa.snippets.color.model.Color;
import kr.or.kosa.snippets.color.service.ColorService;
import kr.or.kosa.snippets.user.service.CustomUserDetails;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    private final S3Service s3Service;
    private final ColorService colorService;

    public SnippetController(
            SnippetService snippetService,
            S3Service s3Service, ColorService colorService) {
        this.snippetService = snippetService;
        this.s3Service     = s3Service;
		this.colorService = colorService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(SnippetTypeBasic.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(SnippetTypeBasic.valueOf(text));
            }
        });
    }

    @GetMapping
    public String getAllSnippets(
            @AuthenticationPrincipal CustomUserDetails details,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        // 1) 로그인 정보 확인
        if (details == null || details.getUserId() == null) {
            return "redirect:/login";
        }

        Long userId = details.getUserId();
        int pageSize = 30;

        // 2) 페이징 시작
        PageHelper.startPage(page, pageSize);

        // 3) 해당 유저 스니펫만 조회 (타입별 내용 포함)
        List<Snippets> list = snippetService.getUserSnippets(userId);
        model.addAttribute("filterUserId", userId);

        // ★ 수정: 타입별 실제 내용으로 contentPreviewMap 생성
        Map<Long, String> previewMap = list.stream()
            .collect(Collectors.toMap(
                Snippets::getSnippetId,
                snippet -> {
                    String preview = "";
                    switch (snippet.getType()) {
                        case CODE:
                        case TEXT:
                            String content = snippet.getContent();
                            if (content != null && !content.trim().isEmpty()) {
                                preview = content.length() > 100 ? content.substring(0, 100) + "…" : content;
                            } else {
                                preview = "내용 없음";
                            }
                            break;
                        case IMG:
                            String altText = snippet.getAltText();
                            if (altText != null && !altText.trim().isEmpty()) {
                                preview = "[이미지] " + altText;
                            } else {
                                preview = "[이미지] 설명 없음";
                            }
                            break;
                        default:
                            preview = "내용 없음";
                    }
                    return preview;
                }
            ));
        model.addAttribute("contentPreviewMap", previewMap);

        // 4) 페이지 정보 생성
        PageInfo<Snippets> pageInfo = new PageInfo<>(list);

        // 5) 페이지 그룹 계산 (1~10, 11~20 …)
        int pageGroupSize = 10;
        int currentPage  = pageInfo.getPageNum();
        int totalPages   = pageInfo.getPages();
        int startPage    = ((currentPage - 1) / pageGroupSize) * pageGroupSize + 1;
        int endPage      = Math.min(startPage + pageGroupSize - 1, totalPages);

        // 6) 뷰로 전달
        model.addAttribute("snippets", list);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("pageGroupSize", pageGroupSize);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        
        // ★ 사이드바 활성화를 위한 activeMenu 설정
        model.addAttribute("activeMenu", "snippets");

        return "basic/snippets/snippetsList";
    }

    @GetMapping("/{snippetId}")
    public String getById(@PathVariable("snippetId") Long snippetId, Model model) {
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        if (type == null) {
            return "error/404";
        }
        Snippets snippet = snippetService.getSnippetsById(snippetId, type);
        if (snippet == null) {
            return "error/404";
        }
        model.addAttribute("snippet", snippet);
        
        // ★ 추가: 스니펫 상세보기도 snippets 메뉴로 간주
        model.addAttribute("activeMenu", "snippets");
        
        return "basic/snippets/snippetDetail";
    }

    @GetMapping("/new")
    public String showAddForm(
            @RequestParam(value="type", required = false) SnippetTypeBasic type,
            Model model,
            @AuthenticationPrincipal CustomUserDetails details) {

        model.addAttribute("snippet", new Snippets());
        
        // ★ 추가: 스니펫 추가 폼도 snippets 메뉴로 간주
        model.addAttribute("activeMenu", "snippets");
        
        // type 파라미터가 있으면 (CODE/TEXT/IMG 선택 후)
        if (type != null) {
            // ① 사용자 기본+커스텀 컬러 조회
            Long userId = details.getUserId();
            List<Color> colors = colorService.getAllAvailableColorsByUserId(userId);
            model.addAttribute("colors", colors);
            // ② 타입별 뷰로 이동
            switch (type) {
                case CODE: return "basic/snippets/snippetAddCode";
                case TEXT: return "basic/snippets/snippetAddText";
                case IMG:  return "basic/snippets/snippetAddImg";
            }
        }
        // 처음엔 타입 선택 페이지
        return "basic/snippets/snippetAddSelect";
    }


    @PostMapping("/new")
    public String addSnippet(
            @AuthenticationPrincipal CustomUserDetails details,  // ① 로그인 정보 주입
            @ModelAttribute Snippets snippet,
            @RequestParam("type") String type,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        
        // ② 로그인 체크 (만약 비로그인 허용 안 하면 생략 가능)
        if (details == null || details.getUserId() == null) {
            return "redirect:/login";
        }
        
        // ③ 스니펫에 userId 세팅
        snippet.setUserId(details.getUserId());
        snippet.setType(SnippetTypeBasic.valueOf(type));
        
        // ④ 이미지 파일 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            String s3Url = s3Service.uploadFile(imageFile);
            snippet.setImageUrl(s3Url);
        }
        
        // ⑤ 삽입 호출 (이제 mapper에 #{userId}가 전달됩니다)
        snippetService.insertSnippets(snippet);
        return "redirect:/snippets";
    }


    @GetMapping("/edit-form/{type}/{snippetId}")
    public String getEditForm(
        @PathVariable(value="type") String type,
        @PathVariable(value="snippetId") Long snippetId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails details) {

        SnippetTypeBasic snippetType;
        try {
            snippetType = SnippetTypeBasic.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "error/404";
        }

        Snippets snippet = snippetService.getSnippetsById(snippetId, snippetType);
        if (snippet == null) return "error/404";

        model.addAttribute("snippet", snippet);

        Long userId = details.getUserId();
        List<Color> colors = colorService.getAllAvailableColorsByUserId(userId);
        model.addAttribute("colors", colors);

        switch (snippetType) {
            case CODE: return "basic/snippets/snippetEditCode";
            case TEXT: return "basic/snippets/snippetEditText";
            case IMG: return "basic/snippets/snippetEditImg";
            default: return "error/404";
        }
    }



    @PostMapping("/edit/{snippetId}")
    public String updateSnippet(
            @PathVariable(value="snippetId") Long snippetId,
            @ModelAttribute Snippets snippet,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        if (type == null) {
            return "error/404";
        }
        Snippets existingSnippet = snippetService.getSnippetsById(snippetId, type);
        if (existingSnippet == null) {
            return "error/404";
        }
        existingSnippet.setSourceUrl(snippet.getSourceUrl());
        existingSnippet.setColorId(snippet.getColorId());
        existingSnippet.setMemo(snippet.getMemo());
        existingSnippet.setVisibility(snippet.getVisibility());
        existingSnippet.setLikeCount(snippet.getLikeCount());
        if (SnippetTypeBasic.TEXT.equals(type)) {
            existingSnippet.setContent(snippet.getContent());
        } else if (SnippetTypeBasic.CODE.equals(type)) {
            existingSnippet.setContent(snippet.getContent());
            existingSnippet.setLanguage(snippet.getLanguage());
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            existingSnippet.setAltText(snippet.getAltText());
            if (imageFile != null && !imageFile.isEmpty()) {
                s3Service.deleteFile(existingSnippet.getImageUrl());
                String newUrl = s3Service.uploadFile(imageFile);
                existingSnippet.setImageUrl(newUrl);
            }
        }
        snippetService.updateSnippets(existingSnippet);
        if (SnippetTypeBasic.CODE.equals(type)) {
            snippetService.updateSnippetCode(existingSnippet);
        } else if (SnippetTypeBasic.TEXT.equals(type)) {
            snippetService.updateSnippetText(existingSnippet);
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            snippetService.updateSnippetImage(existingSnippet);
        }
        
        System.out.println("폼에서 넘어온 colorId = " + snippet.getColorId());
        
        return "redirect:/snippets/" + snippetId;
    }

    @PostMapping("/delete/{snippetId}")
    public String deleteSnippet(@PathVariable(value="snippetId") Long snippetId) {
        snippetService.deleteSnippets(snippetId);
        return "redirect:/snippets";
    }
}
