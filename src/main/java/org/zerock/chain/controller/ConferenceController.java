package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/conference")
@Log4j2
public class ConferenceController {

    // 회의실 메인 페이지
    @GetMapping("/main")
    public String main(Model model) {
        return "conference/main";
    }

    // 회의실 예약 페이지
    @GetMapping("/reservation")
    public String reservation(Model model) {
        return "conference/reservation";
    }

    //  회의실 예약  상세 확인 페이지
    @GetMapping("/detail")
    public String detail(Model model) {
        return "conference/detail";
    }

    //  내 예약 현황 페이지
    @GetMapping("/myList")
    public String myList(Model model) {
        return "conference/myList";
    }

    //  회의실 정보 페이지
    @GetMapping("/room")
    public String room(Model model) {
        return "conference/room";
    }



}
