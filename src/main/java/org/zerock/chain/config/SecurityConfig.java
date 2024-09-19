package org.zerock.chain.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.zerock.chain.service.CustomUserDetailsService;
import org.zerock.chain.model.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                                .requestMatchers("/login", "/api/check-email", "/api/check-phone", "/signup", "/assets/**", "/uploads/**","/admin/attendance/check-in","/admin/attendance/check-out","/admin/attendance/check-status" ).permitAll()
                                .requestMatchers("/notice/register").hasAuthority("공지사항 작성")
                                .requestMatchers("/notice/modify/**").hasAuthority("공지사항 수정")
//                        .requestMatchers("/notice/delete/**").hasAuthority("공지사항 삭제")
                                .requestMatchers("/board/modify/**").hasAuthority("경조사 수정")
                                .requestMatchers("/board/cafeteria/upload").hasAuthority("구내식당 사진 업로드")
                                .requestMatchers("/user/showMakeNotificationModal").hasAuthority("관리자")
                                .requestMatchers("/user/commentModal").hasAuthority("관리자")
                                .requestMatchers("/user/qna/detail/showEditCommentModal").hasAuthority("관리자")
                                .requestMatchers("/admin/**").hasAuthority("관리자") // 관리자 접근 권한이 있는 사용자만 접근 가능
                                .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("emp_no")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .permitAll()
                )
                .rememberMe((rememberMe) -> rememberMe
                        .tokenRepository(persistentTokenRepository()) // remember-me 토큰을 저장할 리포지토리
                        .tokenValiditySeconds(1209600) // remember-me 기능 유지 시간 (2주)
                        .key("uniqueAndSecret") // 고유 키 설정
                        .userDetailsService(customUserDetailsService()) // UserDetailsService 설정
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new InMemoryTokenRepositoryImpl(); // In-Memory 방식으로 remember-me 토큰 저장
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                HttpSession session = request.getSession();
                session.setAttribute("empNo", userDetails.getEmpNo());
                session.setAttribute("firstName", userDetails.getFirstName());
                session.setAttribute("lastName", userDetails.getLastName());
                session.setAttribute("rankName", userDetails.getRankName());
                session.setAttribute("departmentName", userDetails.getDepartmentName()); // 부서 이름 세션에 추가
                response.sendRedirect("/");
            }
        };
    }


    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService();
    }
}
