package org.zerock.chain.controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/personnel")
@Log4j2
public class PersonnelController {


    // 사원 관리 페이지
    @GetMapping("/management")
    public String management() {
        return "admin/personnel/management";
    }


    // 권한 관리 페이지
    @GetMapping("/powerSetting")
    public String powerSetting() {
        return "admin/personnel/powerSetting";
    }

}
