package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.model.Snippettype;
import kr.or.kosa.snippets.basic.service.SnippetService;

@Controller
public class SnippetController {

    private final SnippetService snippetService;

    @Autowired
    public SnippetController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Snippettype.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(Snippettype.valueOf(text));
            }
        });
    }

    @GetMapping("/snippets")
    public String getAllSnippets(Model model) {
        List<Snippets> snippetsList = snippetService.getAllSnippets();
        model.addAttribute("snippets", snippetsList);
        return "basic/snippets/snippetsList";
    }

    @GetMapping("/snippets/{snippetid}")
    public String getSnippetById(
        @PathVariable("snippetid") int snippetid,
        Model model
    ) {
        Snippets basic = snippetService.getSnippetsById(snippetid, null);
        if (basic == null) {
            return "error/404";
        }
        Snippettype type = basic.getType();
        Snippets snippet = snippetService.getSnippetsById(snippetid, type);
        model.addAttribute("snippet", snippet);
        return "basic/snippets/snippetDetail";
    }

    @GetMapping("/snippets/new")
    public String showAddForm(Model model) {
        model.addAttribute("snippet", new Snippets());
        return "basic/snippets/snippetAdd";
    }

    @PostMapping("/snippets/new")
    public String addSnippet(
        @ModelAttribute Snippets snippet,
        @RequestParam("type") String type,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        snippet.setType(Snippettype.valueOf(type));
        if (snippet.getType() == Snippettype.img && imageFile != null && !imageFile.isEmpty()) {
            String url = saveImageFile(imageFile);
            snippet.setImageurl(url);
        }
        snippetService.insertSnippets(snippet);
        return "redirect:/snippets";
    }

    @PostMapping("/snippets/delete/{snippetid}")
    public String deleteSnippet(
        @PathVariable("snippetid") int snippetid
    ) {
        snippetService.deleteSnippets(snippetid);
        return "redirect:/snippets";
    }

    @GetMapping("/snippets/edit/{snippetid}")
    public String editForm(
        @PathVariable("snippetid") int snippetid,
        Model model
    ) {
        Snippettype type = snippetService.getSnippetTypeById(snippetid);
        Snippets snippet = snippetService.getSnippetsById(snippetid, type);
        model.addAttribute("snippet", snippet);
        return "basic/snippets/snippetEdit";
    }

    @PostMapping("/snippets/edit/{snippetid}")
    public String updateSnippet(
        @PathVariable("snippetid") int snippetid,
        @ModelAttribute Snippets snippet,
        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        snippet.setSnippetid(snippetid);
        if (snippet.getType() == Snippettype.img && imageFile != null && !imageFile.isEmpty()) {
            String url = saveImageFile(imageFile);
            snippet.setImageurl(url);
        }
        snippetService.updateSnippets(snippet);
        return "redirect:/snippets/" + snippetid;
    }

    private String saveImageFile(MultipartFile file) throws IOException {
        // 애플리케이션 실행 디렉터리 기준의 uploads/ 폴더
        Path uploadRoot = Paths.get("uploads").toAbsolutePath();
        Files.createDirectories(uploadRoot);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = uploadRoot.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // 브라우저에서는 /uploads/{filename} 으로 접근하도록
        return "/uploads/" + filename;
    }

}
