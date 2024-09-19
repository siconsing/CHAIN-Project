package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@Log4j2
public class AdminController {

    // 관리자 메인 페이지
    @GetMapping("/main")
    public String AdminMain(){ return "/admin/main";}

}
