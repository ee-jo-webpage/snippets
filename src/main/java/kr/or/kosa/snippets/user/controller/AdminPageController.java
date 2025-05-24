package kr.or.kosa.snippets.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping("/ip-blocks")
    public String viewIpBlocks() {
        return "/user/admin/ip-blocks";
    }
}