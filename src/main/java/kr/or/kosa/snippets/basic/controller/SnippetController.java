package kr.or.kosa.snippets.basic.controller;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.UUID;

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

//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;

import kr.or.kosa.snippets.basic.model.SnippetTypeBasic;
import kr.or.kosa.snippets.basic.model.Snippets;
import kr.or.kosa.snippets.basic.service.SnippetService;

@Controller
@RequestMapping("/snippets")
public class SnippetController {

    private final SnippetService snippetService;
    //private final AmazonS3 amazonS3Client;
    //private final String bucket;

    
    public SnippetController(
        SnippetService snippetService
//        ,
//        AmazonS3 amazonS3Client,
//        @Value("${aws.s3.bucket}") String bucket
    ) {
        this.snippetService = snippetService;
//        this.amazonS3Client = amazonS3Client;
//        this.bucket = bucket;
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
        SnippetTypeBasic type = snippetService.getSnippetTypeById(id);
        Snippets snippet = snippetService.getSnippetsById(id, type);
        if (snippet == null) {
            return "error/404";
        }
        model.addAttribute("snippet", snippet);
        return "basic/snippets/snippetDetail";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("snippet", new Snippets());
        return "basic/snippets/snippetAdd";
    }

    @PostMapping("/new")
    public String addSnippet(
        @ModelAttribute Snippets snippet,
        @RequestParam String type,
        @RequestParam(required = false) MultipartFile imageFile
    ) throws IOException {
        snippet.setType(SnippetTypeBasic.valueOf(type));
//        if (snippet.getType() == SnippetTypeBasic.IMG && imageFile != null && !imageFile.isEmpty()) {
//            snippet.setImageUrl(uploadToS3(imageFile));
//        }
        snippetService.insertSnippets(snippet);
        return "redirect:/snippets";
    }

    @PostMapping("/edit/{id}")
    public String updateSnippet(
        @PathVariable("id") Long id,
        @ModelAttribute Snippets snippet,
        @RequestParam(required = false) MultipartFile imageFile
    ) throws IOException {
        snippet.setSnippetId(id);
//        if (snippet.getType() == SnippetTypeBasic.IMG && imageFile != null && !imageFile.isEmpty()) {
//            snippet.setImageUrl(uploadToS3(imageFile));
//        }
        snippetService.updateSnippets(snippet);
        return "redirect:/snippets/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteSnippet(@PathVariable("id") Long id) {
        snippetService.deleteSnippets(id);
        return "redirect:/snippets";
    }

    // S3 업로드 유틸 메서드
//    private String uploadToS3(MultipartFile file) throws IOException {
//        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(file.getContentType());
//        metadata.setContentLength(file.getSize());
//        amazonS3Client.putObject(bucket, key, file.getInputStream(), metadata);
//        return amazonS3Client.getUrl(bucket, key).toString();
//    }
}