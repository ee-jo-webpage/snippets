package kr.or.kosa.snippets.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ComponentController {

    @GetMapping("/components/snippet-section")
    public String getSnippetSection(@RequestParam(required = false) String tagName, Model model) {
        model.addAttribute("selectedTagName", tagName);
        return "snippet-section/snippet-section :: snippetSection";
    }

    @GetMapping("/components/snippet-modal")
    public String getSnippetModal() {
        return "snippet-section/snippet-section :: snippetModal"; // 모달만
    }
}
