package org.zerock.chain.controller;

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

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

@Controller
public class OAuth2Controller {

    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private GoogleAuthorizationCodeFlow flow;


    @PostConstruct
    public void init() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(CREDENTIALS_FILE_PATH));

        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                // 모든 OAuth 범위를 추가
                Arrays.asList(
                        GmailScopes.GMAIL_READONLY,
                        GmailScopes.GMAIL_MODIFY,
                        GmailScopes.GMAIL_METADATA,
                        GmailScopes.GMAIL_COMPOSE,
                        GmailScopes.GMAIL_INSERT,
                        "https://mail.google.com/")
        )
                .setAccessType("offline")
                .build();
    }


    @GetMapping("/oauth2/authorize")
    public String authorize() {
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(getRedirectUri()).build();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam("code") String code) throws IOException {
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(getRedirectUri()).execute();
        Credential credential = flow.createAndStoreCredential(response, "user");
        return "redirect:/threads?accessToken=" + credential.getAccessToken();
    }

    private String getRedirectUri() {
        try (FileReader reader = new FileReader(CREDENTIALS_FILE_PATH)) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
            return clientSecrets.getDetails().getRedirectUris().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load redirect URI from credentials.json", e);
        }
    }

}