package org.zerock.chain.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.chain.model.Signup;
import org.zerock.chain.repository.SignupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.zerock.chain.model.CustomUserDetails;
import org.zerock.chain.dto.ProjectDTO;
import org.zerock.chain.dto.TodoDTO;

import java.util.logging.Logger;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.chain.service.ProjectService;
import org.zerock.chain.service.TodoService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SignupController {

    // java.util.logging.Logger 사용
    private static final Logger logger = Logger.getLogger(SignupController.class.getName());

    private final SignupRepository signupRepository;

    public SignupController(SignupRepository signupRepository) {
        this.signupRepository = signupRepository;
    }

    @Autowired
    private ProjectService projectService;
    @Autowired
    private TodoService todoService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 회원가입 폼을 보여주는 GET 요청 핸들러
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signup", new Signup());
        return "signup";
    }

    // 회원가입 처리 POST 요청 핸들러
    @PostMapping("/signup")
    public String registerSignup(@ModelAttribute Signup signup, RedirectAttributes redirectAttributes) {
        // 입력된 비밀번호를 암호화
        String encodedPassword = passwordEncoder.encode(signup.getPassword());
        signup.setPassword(encodedPassword);

        // dmp_nos는 0, rank_no는 기본값으로 이미 1이 설정됨
        signup.setDmpNo(0L);
        signup.setRankNo(1L);

        // 현재 날짜를 고용일자로 설정
        signup.setHireDate(LocalDate.now());

        // 새로운 회원 정보를 데이터베이스에 저장
        signupRepository.save(signup);

        // 로그 추가
        System.out.println("회원가입 완료 후 사원번호 가져오기");

        Long maxEmpNo = signupRepository.findMaxEmpNo();
        System.out.println("가장 큰 사원번호: " + maxEmpNo);

        if (maxEmpNo != null) {
            String message = "당신의 사원번호는 " + maxEmpNo + "입니다.";
            System.out.println(message); // 로그로 출력
            redirectAttributes.addFlashAttribute("empNoMessage", message);
        } else {
            String message = "회원가입은 완료되었으나 사원번호를 찾을 수 없습니다.";
            System.out.println(message); // 로그로 출력
            redirectAttributes.addFlashAttribute("empNoMessage", message);
        }

        return "redirect:/login";
    }

    // 로그인 페이지를 보여주는 GET 요청 핸들러
    @GetMapping("/login")
    public String loginPage() {

        logger.info("login---------------------");
        // 현재 인증된 사용자가 있는지 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 이미 로그인된 상태라면 메인 페이지로 리다이렉트
            return "redirect:/";
        }
        // 로그인 페이지로 이동
        return "login";
    }

    // 메인 페이지를 보여주는 GET 요청 핸들러
    @GetMapping("/")
    public String home(Authentication authentication, HttpSession session, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long empNo = userDetails.getEmpNo();

            // 세션에 로그인 정보를 저장
            session.setAttribute("empNo", empNo);
            session.setAttribute("firstName", userDetails.getFirstName());
            session.setAttribute("lastName", userDetails.getLastName());
            session.setAttribute("rankName", userDetails.getRankName());
            session.setAttribute("rankName", userDetails.getRankName());

            // 모델에 추가하여 뷰로 전달
            model.addAttribute("empNo", empNo);
            model.addAttribute("firstName", userDetails.getFirstName());
            model.addAttribute("lastName", userDetails.getLastName());
            model.addAttribute("rankName", userDetails.getRankName());

            // 프로젝트 데이터 가져오기 (사용자의 프로젝트) 박성은 추가
            List<ProjectDTO> projects = projectService.getProjectsByUser(empNo); // 프로젝트 서비스에서 데이터 호출
            model.addAttribute("projects", projects);

            // todo 데이터 가져오기 (사용자) 박성은 추가
            List<TodoDTO> favoriteTodos = todoService.getFavoriteTodos(); // 즐겨찾기 목록
            model.addAttribute("favoriteTodos", favoriteTodos);  // 즐겨찾기 목록을 모델에 추가

        }
        return "index"; // index.html 템플릿으로 이동
    }

    // 이메일 중복 체크 API
    @GetMapping("/api/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam("email") String email) {
        // 입력된 이메일을 트림 처리하고 대소문자 구분 없이 검색
        String trimmedEmail = email.trim();
        logger.info("Checking email: " + trimmedEmail);  // 로그 추가

        boolean exists = signupRepository.findByEmailIgnoreCase(trimmedEmail).isPresent();
        logger.info("Email exists: " + exists);  // 로그로 결과 확인
        return ResponseEntity.ok(exists);
    }

    // 전화번호 중복 체크 API
    @GetMapping("/api/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam("phone") String phone) {
        // 입력된 전화번호를 트림 처리
        String trimmedPhone = phone.trim();
        logger.info("Checking phone: " + trimmedPhone);  // 로그 추가

        boolean exists = signupRepository.findByPhoneNum(trimmedPhone).isPresent();
        logger.info("Phone exists: " + exists);  // 로그로 결과 확인
        return ResponseEntity.ok(exists);
    }

}
