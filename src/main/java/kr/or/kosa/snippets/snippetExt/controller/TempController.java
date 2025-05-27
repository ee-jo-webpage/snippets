package kr.or.kosa.snippets.snippetExt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TempController {

    @GetMapping("/main")
    public String main(){
        return "main";
    }
}
