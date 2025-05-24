package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
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

import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    private final String uploadDir;

    public SnippetController(SnippetService snippetService, @Value("${file.upload-dir}") String uploadDir) {
        this.snippetService = snippetService;
        this.uploadDir = uploadDir;
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
    public String getAllSnippets(Model model) {
        model.addAttribute("snippets", snippetService.getAllSnippets());
        return "basic/snippets/snippetsList";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") Long id, Model model) {
        // 1) 먼저 DB에서 type 조회
        SnippetTypeBasic type = snippetService.getSnippetTypeById(id);
        // 2) type이 null이면 404 페이지로
        if (type == null) {
            return "error/404";
        }
        // 3) 안전하게 상세조회
        Snippets snippet = snippetService.getSnippetsById(id, type);
        if (snippet == null) {
            return "error/404";
        }
        model.addAttribute("snippet", snippet);
        return "basic/snippets/snippetDetail";
    }

    @GetMapping("/new")
    public String showAddForm(
            @RequestParam(value = "type", required = false) SnippetTypeBasic type,
            Model model) {
        model.addAttribute("snippet", new Snippets());
        
        if (type == null) {
            return "basic/snippets/snippetAddSelect";
        }
        
        switch (type) {
        case CODE: return "basic/snippets/snippetAddCode";
        case TEXT: return "basic/snippets/snippetAddText";
        case IMG:  return "basic/snippets/snippetAddImg";
        default:   return "error/404";
    }
        
    }

    @PostMapping("/new")
    public String addSnippet(@ModelAttribute Snippets snippet, @RequestParam("type") String type,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        snippet.setType(SnippetTypeBasic.valueOf(type));

        // 이미지 파일이 있을 경우 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 이미지 URL 설정
            snippet.setImageUrl("/uploads/" + fileName);
        }

        snippetService.insertSnippets(snippet);
        return "redirect:/snippets";
    }

    @GetMapping("/edit/{snippetId}")
    public String showEditForm(@PathVariable("snippetId") Long snippetId, Model model) {
        // 1) type 조회
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        if (type == null) {
            return "error/404"; // type이 null일 경우 404 페이지로
        }

        // 2) 스니펫 상세 조회
        Snippets snippet = snippetService.getSnippetsById(snippetId, type);
        if (snippet == null) {
            return "error/404"; // 스니펫이 존재하지 않으면 404 페이지로
        }

        // 3) 모델에 스니펫 추가
        model.addAttribute("snippet", snippet);

        // 4) type에 맞는 수정 페이지로 리디렉션
        if (SnippetTypeBasic.CODE.equals(type)) {
            return "basic/snippets/snippetEditCode"; // 코드 수정 페이지
        } else if (SnippetTypeBasic.TEXT.equals(type)) {
            return "basic/snippets/snippetEditText"; // 텍스트 수정 페이지
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            return "basic/snippets/snippetEditImg"; // 이미지 수정 페이지
        }

        return "error/404"; // type이 맞지 않으면 404 페이지로 리디렉션
    }

    @PostMapping("/edit/{snippetId}")
    public String updateSnippet(@PathVariable("snippetId") Long snippetId, @ModelAttribute Snippets snippet,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        // 1) type을 먼저 조회
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        if (type == null) {
            return "error/404";
        }

        // 2) 기존 스니펫을 가져오기
        Snippets existingSnippet = snippetService.getSnippetsById(snippetId, type);
        if (existingSnippet == null) {
            return "error/404";
        }

        // 3) 기존 데이터에 수정된 내용 덮어쓰기
        existingSnippet.setSourceUrl(snippet.getSourceUrl());
        existingSnippet.setMemo(snippet.getMemo());
        existingSnippet.setVisibility(snippet.getVisibility());
        existingSnippet.setLikeCount(snippet.getLikeCount());

        // 4) 타입에 맞게 content 설정
        if (SnippetTypeBasic.TEXT.equals(type)) {
            existingSnippet.setContent(snippet.getContent());
        } else if (SnippetTypeBasic.CODE.equals(type)) {
            existingSnippet.setContent(snippet.getContent());
            existingSnippet.setLanguage(snippet.getLanguage());
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            existingSnippet.setAltText(snippet.getAltText());
            
            // 5) 이미지 파일 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                // 기존 이미지 파일이 있다면 삭제
                if (existingSnippet.getImageUrl() != null) {
                    String oldFileName = existingSnippet.getImageUrl().substring(existingSnippet.getImageUrl().lastIndexOf("/") + 1);
                    Path oldFilePath = Paths.get(uploadDir, oldFileName);
                    Files.deleteIfExists(oldFilePath);
                }

                // 새 이미지 파일 저장
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                Path targetLocation = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // 이미지 URL 업데이트
                existingSnippet.setImageUrl("/uploads/" + fileName);
            }
        }

        // 6) 수정된 스니펫을 서비스로 전달하여 업데이트
        snippetService.updateSnippets(existingSnippet);

        // 7) 타입에 맞게 자식 테이블 업데이트
        if (SnippetTypeBasic.CODE.equals(type)) {
            snippetService.updateSnippetCode(existingSnippet);
        } else if (SnippetTypeBasic.TEXT.equals(type)) {
            snippetService.updateSnippetText(existingSnippet);
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            snippetService.updateSnippetImage(existingSnippet);
        }

        return "redirect:/snippets/" + snippetId;
    }

    @PostMapping("/delete/{id}")
    public String deleteSnippet(@PathVariable("id") Long id) {
        snippetService.deleteSnippets(id);
        return "redirect:/snippets"; // 삭제 후 목록으로만 리다이렉트
    }
}
