package org.zerock.chain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    /*@GetMapping("/")
    public String index() {
        return "index"; // templates/index.html 파일을 서빙
    }*/

    @GetMapping("/example")
    public String example() {
        return "example"; // templates/example.html 파일을 서빙
    }

    @GetMapping("/organizationChart")
    public String organization_chart() {
        return "organizationChart"; // templates/organizationChart.html 파일을 서빙
    }

    // 로그아웃 페이지로 이동
    @GetMapping("/logout")
    public String userLogout(Model model) {
        return "/logout";
    }

/*    // 로그인 페이지로 이동
    @GetMapping("/login")
    public String userLogin(Model model) {
        return "/login";
    }

    // 회원가입 페이지로 이동
    @GetMapping("/signup")
    public String createAccount(Model model) {return "/signup";}*/
}
