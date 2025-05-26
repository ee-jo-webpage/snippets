package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.List;

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
import kr.or.kosa.snippets.basic.service.S3Service;
import kr.or.kosa.snippets.basic.service.SnippetService;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    private final S3Service s3Service;

    public SnippetController(
            SnippetService snippetService,
            S3Service s3Service) {
        this.snippetService = snippetService;
        this.s3Service     = s3Service;
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
            @RequestParam(value = "userId", required = false) Long userId,
            Model model) {

        List<Snippets> list;
        if (userId != null) {
            list = snippetService.getUserSnippets(userId);
            model.addAttribute("filterUserId", userId);
        } else {
            list = snippetService.getAllSnippets();
        }

        model.addAttribute("snippets", list);
        return "basic/snippets/snippetsList";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable("id") Long id, Model model) {
        SnippetTypeBasic type = snippetService.getSnippetTypeById(id);
        if (type == null) {
            return "error/404";
        }
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
    public String addSnippet(
            @ModelAttribute Snippets snippet,
            @RequestParam("type") String type,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        snippet.setType(SnippetTypeBasic.valueOf(type));
        if (imageFile != null && !imageFile.isEmpty()) {
            String s3Url = s3Service.uploadFile(imageFile);
            snippet.setImageUrl(s3Url);
        }
        snippetService.insertSnippets(snippet);
        return "redirect:/snippets";
    }

    @GetMapping("/edit/{snippetId}")
    public String showEditForm(@PathVariable(value="snippetId") Long snippetId, Model model) {
        SnippetTypeBasic type = snippetService.getSnippetTypeById(snippetId);
        if (type == null) {
            return "error/404";
        }
        Snippets snippet = snippetService.getSnippetsById(snippetId, type);
        if (snippet == null) {
            return "error/404";
        }
        model.addAttribute("snippet", snippet);
        if (SnippetTypeBasic.CODE.equals(type)) {
            return "basic/snippets/snippetEditCode";
        } else if (SnippetTypeBasic.TEXT.equals(type)) {
            return "basic/snippets/snippetEditText";
        } else if (SnippetTypeBasic.IMG.equals(type)) {
            return "basic/snippets/snippetEditImg";
        }
        return "error/404";
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
        return "redirect:/snippets/" + snippetId;
    }

    @PostMapping("/delete/{id}")
    public String deleteSnippet(@PathVariable Long id) {
        snippetService.deleteSnippets(id);
        return "redirect:/snippets";
    }
}
