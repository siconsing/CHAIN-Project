package org.zerock.chain.controller;
import com.google.api.client.auth.oauth2.TokenResponseException;
import org.springframework.core.io.ClassPathResource;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller
public class OAuth2Controller {

    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);

    private GoogleAuthorizationCodeFlow flow;


    @PostConstruct
    public void init() throws Exception {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Classpath 리소스를 사용하여 credentials.json을 로드
            GoogleClientSecrets clientSecrets;
            try (InputStream in = new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream()) {
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            }

            flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets,        Set.of(
                    GmailScopes.GMAIL_READONLY,  // Gmail 읽기 전용 권한
                    GmailScopes.GMAIL_MODIFY,    // Gmail 수정 권한
                    GmailScopes.GMAIL_COMPOSE,   // 이메일 작성 권한
                    GmailScopes.GMAIL_INSERT,    // 이메일 삽입 권한
                    "https://mail.google.com/"   // 전체 메일 관련 권한
            ))
                    .setAccessType("offline")
                    .build();

        } catch (Exception e) {
            // 예외 발생 시 메시지 출력 및 로깅
            System.err.println("Error during OAuth2Controller initialization: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 예외를 다시 던져 스프링에서 처리하게 함
        }
    }




}