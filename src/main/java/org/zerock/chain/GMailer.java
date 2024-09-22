package org.zerock.chain;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import java.io.ByteArrayOutputStream;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;

import static javax.mail.Message.RecipientType.TO;

public class GMailer {
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
    @Value("${gmail.redirect.uri}")
    private String gmailRedirectUri;

    // 테스트용 이메일 주소를 정의합니다
    private static final String TEST_EMAIL = "yiit1333@gmail.com";
    // Gmail 서비스 객체를 저장할 변수를 정의합니다.
    private final Gmail service;

    // 생성자: Gmail 서비스를 초기화합니다.
    public GMailer() throws Exception {
        // HTTP 트랜스포트를 초기화합니다.
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // JSON 팩토리를 초기화합니다.
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        // Gmail 서비스 객체를 빌드합니다.
        service = new Gmail.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, jsonFactory))
                .setApplicationName("chain")
                .build();
    }

    // 사용자의 자격 증명을 얻는 메서드입니다.
    private Credential getCredentials(final NetHttpTransport httpTransport, GsonFactory jsonFactory)
            throws IOException {
        // 클라이언트 시크릿을 로드합니다.
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(GMailer.class.getResourceAsStream("/credentials.json")));

        // 인증 흐름을 구축하고 사용자 승인 요청을 트리거합니다.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets,         Set.of(
                GmailScopes.GMAIL_READONLY,  // Gmail 읽기 전용 권한
                GmailScopes.GMAIL_MODIFY,    // Gmail 수정 권한
                GmailScopes.GMAIL_COMPOSE,   // 이메일 작성 권한
                GmailScopes.GMAIL_INSERT,    // 이메일 삽입 권한
                "https://mail.google.com/"   // 전체 메일 관련 권한
        ))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();

        // 인증 URL을 생성하고 클라이언트 또는 브라우저로 전달 (외부 도메인 사용)
        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(gmailRedirectUri);
        System.out.println("Please open the following URL in your browser:");
        System.out.println(authorizationUrl);

        // 사용자로부터 받은 인증 코드를 사용하여 액세스 토큰을 가져옴
        String code = receiveAuthorizationCode();
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(gmailRedirectUri).execute();

        return flow.createAndStoreCredential(response, "user");
    }
    // 리디렉션 URI에서 인증 코드를 수신하는 메서드 구현
    private static String receiveAuthorizationCode() {
        // 사용자로부터 인증 코드를 입력받는 로직 구현 (또는 서버의 콜백 처리 로직)
        return "authorization_code_from_user";  // 사용자가 브라우저에서 입력한 코드를 받아야 함
    }


    // 이메일을 보내는 메서드입니다. 수신자의 이메일 주소를 매개변수로 추가합니다.
    private void sendMail(String recipientEmail, String subject, String message) throws Exception {
        // 이메일 세션 설정을 위한 프로퍼티 객체를 생성합니다.
        Properties props =  System.getProperties();
        String host = "smtp.gmail.com";
        props.setProperty("smtp.smtp.host", host);
        Session session = Session.getDefaultInstance(props, null);
        // MIME 형식의 이메일 객체를 생성합니다.
        MimeMessage email = new MimeMessage(session);
        // 발신자 이메일 주소를 설정합니다.
        email.setFrom(new InternetAddress(TEST_EMAIL));
        // 수신자 이메일 주소를 설정합니다.
        email.addRecipient(TO, new InternetAddress(recipientEmail));
        // 이메일 제목을 설정합니다.
        email.setSubject(subject);
        // 이메일 본문을 설정합니다.
        email.setText(message);

        // 이메일을 바이트 배열로 변환하여 인코딩합니다.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        // Gmail API의 메시지 객체를 생성하고 인코딩된 이메일을 설정합니다.
        Message msg = new Message();
        msg.setRaw(encodedEmail);

        try {
            // Gmail API를 통해 메시지를 전송합니다.
            msg = service.users().messages().send("me", msg).execute();
            // 전송된 메시지의 ID를 출력합니다.
            System.out.println("Message id: " + msg.getId());
            // 전송된 메시지의 상세 정보를 출력합니다.
            System.out.println(msg.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            // 전송 중 예외가 발생하면 에러 코드를 확인하고 처리합니다.
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                // 권한 문제가 발생하면 에러 메시지를 출력합니다.
                System.err.println("Unable to send message: " + e.getDetails());
            } else {
                // 기타 예외는 다시 던집니다.
                throw e;
            }
        }
    }
    // 메인 메서드: GMailer 인스턴스를 생성하고 이메일을 보냅니다.
    public static void main(String[] args) throws Exception {
        // GMailer 객체를 생성하고 sendMail 메서드를 호출하여 이메일을 보냅니다.
        new GMailer().sendMail("yiit1333@gmail.com", "새로운 메시지", """
                Dear ,
                Hello world
                """);
    }
}
