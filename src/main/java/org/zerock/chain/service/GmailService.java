package org.zerock.chain.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.json.JsonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Label;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.chain.dto.MessageDTO;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@Service
@Log4j2
public class GmailService {

    // static 변수로 Singleton 인스턴스 선언
    private static Gmail service;
    private static GoogleAuthorizationCodeFlow flow;
    private static Credential cachedCredential;
    private static long credentialExpiryTime;
    private static final long CACHE_DURATION_MS = 3600 * 1000; // 1시간 (3600초)
    private static final String UPLOAD_DIR = "C:/upload/";
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${gmail.redirect.uri}")
    private String gmailRedirectUri;
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";


    // Singleton 패턴 적용: 외부에서 객체 생성을 하지 못하도록 기본 생성자를 private으로 설정
    private GmailService() {
    }

    // Singleton 인스턴스를 가져오는 메서드
    public synchronized Gmail getInstance() {
        if (service == null) {
            try {
                log.info("Gmail 서비스 초기화 중...");
                NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                // credentials.json 파일에서 클라이언트 비밀 정보 로드
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                        new InputStreamReader(GmailService.class.getResourceAsStream("/credentials.json")));

                flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, jsonFactory, clientSecrets, Set.of(
                        GmailScopes.GMAIL_READONLY,  // Gmail 읽기 전용 권한
                        GmailScopes.GMAIL_MODIFY,    // Gmail 수정 권한
                        GmailScopes.GMAIL_COMPOSE,   // 이메일 작성 권한
                        GmailScopes.GMAIL_INSERT,    // 이메일 삽입 권한
                        "https://mail.google.com/"   // 전체 메일 관련 권한
                ))
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens"))) // 로컬에 토큰 저장
                        .setAccessType("offline") // 오프라인 접근 허용
                        .build();

                log.info("getInstacne-------------> 크레덴셜{}", cachedCredential);

                service = new Gmail.Builder(httpTransport, jsonFactory, cachedCredential)
                        .setApplicationName("chain")
                        .build();

                log.info("Gmail 서비스 초기화 완료.");
            } catch (Exception e) {
                log.error("Gmail 서비스 초기화 중 오류 발생", e);
                throw new RuntimeException("Gmail 서비스 초기화 실패", e);
            }
        }
        return service;
    }

       // 콜백에서 받은 인증 코드를 처리하는 메서드
    public void processAuthorizationCode(String code) throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        log.info("발급받은 코드 {}", code);


        // 인증 코드로 토큰 요청
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(gmailRedirectUri).execute();

        // 자격 증명 생성 및 저장
        cachedCredential = flow.createAndStoreCredential(response, "user");
        log.info("발급받은 cachedCredential코드 {}", cachedCredential);

        log.info("새로운 자격 증명이 생성되었습니다.");
    }

    @GetMapping("/oauth2/authorize")
    public String authorize() {
        System.out.println("---------------------->" + getRedirectUri());
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(gmailRedirectUri).build();
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam("code") String code) throws IOException {
        try {
            System.out.println("+++++++++++++++++++++++++++++++++>" + getRedirectUri());
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(getRedirectUri()).execute();
            Credential credential = flow.createAndStoreCredential(response, "user");
            if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
                credential.refreshToken();
            }

            // GmailService에서 인증 코드로 자격 증명을 처리
            processAuthorizationCode(code);

            return "redirect:/mail/threads?accessToken=" + credential.getAccessToken();
        } catch (TokenResponseException e) {
            // 오류 처리
            System.out.println("Error during token exchange: " + e.getDetails());
            return "error";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRedirectUri() {
        try (InputStream in = new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream()) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            return clientSecrets.getDetails().getRedirectUris().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load redirect URI from credentials.json", e);
        }
    }

    // 캐시된 자격 증명이 유효한지 확인하는 메서드
    private boolean isCachedCredentialValid() {
        return cachedCredential != null &&
                cachedCredential.getAccessToken() != null &&
                credentialExpiryTime > System.currentTimeMillis();
    }

    // 브라우저에서 OAuth2 인증 URL을 여는 메서드
    private void openAuthUrlInBrowser(String authUrl) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));  // 브라우저에 URL 열기
                System.out.println("브라우저에서 URL을 열었습니다: " + authUrl);
            } else {
                openUrlByCommand(authUrl);  // 브라우저가 지원되지 않으면 명령어로 실행
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("브라우저에서 URL을 여는 도중 오류 발생: " + e.getMessage());
        }
    }

    private void openUrlByCommand(String authUrl) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        if (os.contains("win")) {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + authUrl);
        } else if (os.contains("mac")) {
            rt.exec("open " + authUrl);
        } else if (os.contains("nix") || os.contains("nux")) {
            String[] browsers = {"xdg-open", "google-chrome", "firefox"};
            for (String browser : browsers) {
                try {
                    rt.exec(new String[]{browser, authUrl});
                    break;
                } catch (IOException e) {
                    // 시도 실패 시 다음 브라우저로 넘어감
                }
            }
        }
    }


    public void sendMail(String recipientEmail, String subject, String messageText, List<String> filePaths) throws Exception {
        Gmail service = getInstance();

        log.info("Sending email to: {}", recipientEmail);
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            // 수신자 이메일을 콤마로 구분된 리스트로 분리
            String[] recipients = recipientEmail.split(",");

            for (String recipient : recipients) {
                MimeMessage email = new MimeMessage(session);

                email.setFrom(new InternetAddress("yiit1333@gmail.com"));
                email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient.trim())); // 각 수신자에게 메일 전송
                email.setSubject(subject);

                // 본문 설정 및 첨부파일 처리
                Multipart multipart = new MimeMultipart();

                // 본문 설정 (HTML 포맷)
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setContent(messageText, "text/html; charset=UTF-8");
                multipart.addBodyPart(textPart);

                // 첨부파일 처리 로직 (이중으로 추가되지 않도록 수정)
                if (filePaths != null && !filePaths.isEmpty()) {
                    Set<String> uniqueFilePaths = new HashSet<>(filePaths); // 중복 제거
                    for (String filePath : uniqueFilePaths) {
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        DataSource source = new FileDataSource(filePath);
                        attachmentPart.setDataHandler(new DataHandler(source));
                        attachmentPart.setFileName(Paths.get(filePath).getFileName().toString());
                        multipart.addBodyPart(attachmentPart);
                    }
                }

                email.setContent(multipart);

                // 이메일을 바이트 배열로 변환한 후 Base64로 인코딩
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                email.writeTo(buffer);
                String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());

                // Gmail API를 사용하여 이메일 전송
                Message message = new Message();
                message.setRaw(encodedEmail);
                service.users().messages().send("me", message).execute();

                log.info("Email sent successfully to: {}", recipient.trim());
            }
        } catch (MessagingException e) {
            log.error("Failed to create email message: {}", e.getMessage());
            throw new Exception("Failed to create email message", e);
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new Exception("Failed to send email", e);
        }
    }


    public Optional<String> getHeader(List<MessagePartHeader> headers, String name) {
        return headers.stream()
                .filter(header -> header.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst();
    }


    public List<MessageDTO> listMessages(String userId) throws IOException {
        Gmail service = getInstance();
        List<Message> messages = service.users().messages().list(userId)
                .setFields("messages(id)") // 메일 ID만 가져오도록 제한
                .execute().getMessages();

        List<MessageDTO> messageDTOList = new ArrayList<>();

        for (Message message : messages) {
            Message summaryMessage = service.users().messages().get(userId, message.getId())
                    .setFields("id,payload(headers)") // 헤더 정보만 가져옴
                    .execute();
            MessagePart payload = summaryMessage.getPayload();

            if (payload != null) {
                List<MessagePartHeader> headers = payload.getHeaders();

                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setId(summaryMessage.getId());
                messageDTO.setFrom(getHeader(headers, "From").orElse("Unknown"));
                messageDTO.setSubject(getHeader(headers, "Subject").orElse("No Subject"));
                messageDTO.setDate(getHeader(headers, "Date").orElse("Unknown Date"));

                boolean isStarred = summaryMessage.getLabelIds() != null && summaryMessage.getLabelIds().contains("STARRED");
                messageDTO.setStarred(isStarred);

                messageDTOList.add(messageDTO);
            }
        }
        return messageDTOList;
    }


    // 주어진 사용자 ID와 메시지 ID를 사용해 Gmail API에서 이메일 메시지를 가져옵니다.
    public Message getMessage(String userId, String messageId) throws IOException {
        return service.users().messages().get(userId, messageId).execute();
    }

    // 이메일 메시지의 본문 내용을 가져오는 메서드
// 이메일 메시지를 가져와서 그 안의 HTML 본문을 추출한 후, 이미지 CID를 실제 이미지 경로로 변환합니다.
    public String getMessageContent(String userId, String messageId) throws IOException {
        Message message = getMessage(userId, messageId); // 메시지를 가져옴
        MessagePart payload = message.getPayload(); // 메시지의 페이로드를 가져옴
        StringBuilder body = new StringBuilder(); // 본문을 저장할 StringBuilder 초기화

        if (payload != null) {
            String htmlContent = getHtmlContent(payload); // HTML 콘텐츠를 추출
            if (htmlContent != null) {
                body.append(htmlContent); // HTML 내용을 본문에 추가
            } else {
                log.warn("이메일 메시지에서 HTML 내용을 찾을 수 없습니다."); // HTML 본문이 없을 경우 경고 로그
            }
        }

        // 이미지 CID 변환 작업 수행
        String finalContent = body.toString(); // 본문을 문자열로 변환
        Map<String, String> cidMap = extractCidMap(payload); // CID와 이미지 경로의 매핑 추출
        finalContent = parseEmailContent(finalContent, cidMap); // CID를 실제 경로로 변환

        return finalContent; // 변환된 최종 본문 반환
    }

    // MIME 파트를 재귀적으로 처리하여 HTML 콘텐츠를 추출하는 메서드
// 주어진 메시지 파트를 검사하여, HTML 콘텐츠를 추출합니다.
    private String getHtmlContent(MessagePart part) throws IOException {
        String mimeType = part.getMimeType(); // MIME 타입을 가져옴
        MessagePartBody partBody = part.getBody(); // MIME 파트의 본문을 가져옴

        // MIME 타입이 text/html인 경우, 그 데이터를 디코딩하여 반환
        if (mimeType.equals("text/html")) {
            if (partBody != null && partBody.getData() != null) {
                return new String(Base64.decodeBase64(partBody.getData()), StandardCharsets.UTF_8);
            } else {
                log.warn("HTML 파트에 데이터가 없습니다."); // 데이터가 없을 경우 경고 로그
            }
        } else if (mimeType.startsWith("multipart/")) {
            return extractHtmlFromMultipart(part); // multipart/ 관련 MIME 타입의 경우, 재귀적으로 HTML 콘텐츠를 추출
        }
        return null; // HTML 콘텐츠를 찾지 못한 경우 null 반환
    }

    // Multipart 내에서 HTML 콘텐츠를 추출하는 메서드
// 주어진 multipart 파트를 재귀적으로 처리하여 HTML 콘텐츠를 추출합니다.
    private String extractHtmlFromMultipart(MessagePart part) throws IOException {
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String result = getHtmlContent(subPart); // 각 서브 파트를 검사하여 HTML 콘텐츠를 추출
                if (result != null) {
                    return result; // HTML 콘텐츠를 찾으면 반환
                }
            }
        } else {
            log.warn("Multipart 파트에 서브 파트가 없습니다."); // 서브 파트가 없을 경우 경고 로그
        }
        return null; // HTML 콘텐츠를 찾지 못한 경우 null 반환
    }

    // CID와 이미지 경로의 매핑을 추출하는 메서드
// 메시지 페이로드를 검사하여, 이미지 CID와 그에 대응하는 경로를 매핑한 맵을 생성합니다.
    private Map<String, String> extractCidMap(MessagePart payload) throws IOException {
        Map<String, String> cidMap = new HashMap<>();

        if (payload != null) {
            log.info("CID 추출을 위한 Payload 처리 중...");
            processCidMap(payload, cidMap); // 페이로드를 처리하여 CID와 이미지 경로를 매핑
            log.info("CID 추출 완료: {}", cidMap); // 완료된 CID 맵을 로그로 출력
        } else {
            log.warn("Payload가 null이므로 CID 추출을 건너뜀."); // 페이로드가 null일 경우 경고 로그
        }

        return cidMap; // CID 맵 반환
    }

    // CID와 이미지 경로의 매핑을 처리하는 메서드
// 주어진 메시지 파트를 검사하여, CID와 이미지 경로를 매핑합니다.
    private void processCidMap(MessagePart part, Map<String, String> cidMap) throws IOException {
        String mimeType = part.getMimeType(); // MIME 타입을 가져옴
        MessagePartBody partBody = part.getBody(); // MIME 파트의 본문을 가져옴

        log.info("MIME 타입 처리 중: {}", mimeType);

        if (mimeType.startsWith("image/")) {
            processImagePartForCidMap(part, cidMap, mimeType, partBody); // 이미지 파트의 경우 CID와 경로를 처리
        } else if (mimeType.startsWith("multipart/")) {
            processMultipartForCidMap(part, cidMap); // multipart 파트의 경우 서브 파트를 재귀적으로 처리
        } else {
            log.warn("처리되지 않은 MIME 타입: {}. 파트 스킵.", mimeType); // 처리되지 않는 MIME 타입의 경우 경고 로그
        }
    }

    // CID와 이미지 경로의 매핑을 처리하는 메서드
// 이미지 MIME 타입의 메시지 파트를 처리하여 CID와 이미지 경로를 매핑합니다.
    private void processImagePartForCidMap(MessagePart part, Map<String, String> cidMap, String mimeType, MessagePartBody partBody) throws IOException {
        if (partBody != null) {
            log.info("이미지 파트 Body가 null이 아님. 데이터 확인 중...");
            if (partBody.getData() != null) {
                log.info("이미지 데이터 존재. CID 처리 중...");
                String cid = extractCid(part); // CID를 추출
                String imageUrl = saveImageToFileSystem(partBody.getData(), mimeType, cid); // 이미지를 파일 시스템에 저장하고 URL을 반환
                cidMap.put(cid, imageUrl); // CID와 이미지 경로를 매핑
                log.info("CID: {}가 이미지 경로로 매핑됨: {}", cid, imageUrl);
            } else {
                log.warn("이미지 파트에 데이터가 없음. Attachment ID: {}", partBody.getAttachmentId());
                handleImageAttachmentForCidMap(part, cidMap, mimeType, partBody); // 이미지 첨부파일 처리
            }
        } else {
            log.warn("이미지 파트 Body가 null입니다."); // Body가 null일 경우 경고 로그
        }
    }

    // 이미지 첨부파일을 처리하여 CID와 경로를 매핑하는 메서드
// 이미지 첨부파일을 가져와서 CID와 경로를 매핑합니다.
    private void handleImageAttachmentForCidMap(MessagePart part, Map<String, String> cidMap, String mimeType, MessagePartBody partBody) throws IOException {
        if (partBody.getAttachmentId() != null) {
            log.info("Attachment ID: {}에 대한 첨부파일 데이터 가져오는 중...", partBody.getAttachmentId());
            // 첨부파일 데이터 가져오기 시도
            MessagePartBody attachPart = service.users().messages().attachments()
                    .get("me", part.getPartId(), partBody.getAttachmentId()).execute();

            if (attachPart.getData() != null) {
                log.info("첨부파일 데이터 가져오기 성공. CID 처리 중...");
                String cid = extractCid(part); // CID를 추출
                String imageUrl = saveImageToFileSystem(attachPart.getData(), mimeType, cid); // 이미지를 파일 시스템에 저장하고 URL을 반환
                cidMap.put(cid, imageUrl); // CID와 이미지 경로를 매핑
                log.info("CID: {}가 이미지 경로로 매핑됨: {}", cid, imageUrl);
            } else {
                log.warn("Attachment ID: {}에 대한 첨부파일 데이터를 가져오지 못했습니다.", partBody.getAttachmentId());
            }
        }
    }

    // Multipart 파트를 처리하여 CID와 이미지 경로를 추출하는 메서드
// multipart 파트를 재귀적으로 처리하여 CID와 이미지 경로를 추출합니다.
    private void processMultipartForCidMap(MessagePart part, Map<String, String> cidMap) throws IOException {
        if (part.getParts() != null) {
            log.info("Multipart 콘텐츠 {}개 처리 중", part.getParts().size());
            for (MessagePart subPart : part.getParts()) {
                processCidMap(subPart, cidMap); // 각 서브 파트를 처리하여 CID와 경로를 매핑
            }
        } else {
            log.warn("Multipart 파트에 서브 파트가 없습니다."); // 서브 파트가 없을 경우 경고 로그
        }
    }

    // CID(Content-ID)를 추출하는 메서드
// 메시지 파트의 헤더에서 CID(Content-ID)를 추출합니다.
    private String extractCid(MessagePart part) {
        String cid = "";
        if (part.getHeaders() != null) {
            for (MessagePartHeader header : part.getHeaders()) {
                if ("Content-ID".equalsIgnoreCase(header.getName())) {
                    cid = header.getValue(); // Content-ID 헤더의 값을 CID로 설정
                    break;
                }
            }
        }
        // CID에서 불필요한 문자 제거
        return cid.replace("<", "").replace(">", "").replaceAll("[^a-zA-Z0-9]", "_");
    }

    // 이미지 데이터를 파일 시스템에 저장하고 경로를 반환하는 메서드
// CID를 기반으로 파일 이름을 생성하여 이미지 데이터를 파일 시스템에 저장합니다.
    private String saveImageToFileSystem(String imageData, String mimeType, String cid) throws IOException {
        byte[] imageBytes = Base64.decodeBase64(imageData);
        String extension = mimeType.split("/")[1];

        String cleanCid = cid.replaceAll("_cweb[0-9]+_nm", "");
        String imageName = "image_" + cleanCid + "." + extension;

        // 실제 파일 시스템 경로 설정 - C:/upload/로 변경
        Path imagePath = Paths.get("C:/upload/", imageName);

        log.info("CID: {}에 대한 이미지를 경로: {}에 저장합니다.", cid, imagePath.toAbsolutePath());

        try {
            Files.createDirectories(imagePath.getParent());
            Files.write(imagePath, imageBytes);
            log.info("이미지 경로에 저장됨: {}", imagePath.toAbsolutePath().toString());

            if (Files.exists(imagePath)) {
                log.info("이미지 파일이 존재함: {}", imagePath.toAbsolutePath().toString());
            } else {
                log.error("이미지 파일이 저장 후에도 발견되지 않음.");
            }
        } catch (IOException e) {
            log.error("이미지 파일 저장 실패: {}", imagePath.toAbsolutePath().toString(), e);
            throw e;
        }

        // 이메일 본문에서 참조할 웹 경로 반환
        return "/upload/" + imageName;
    }


    // 이메일 본문 내의 CID를 실제 이미지 경로로 대체하는 메서드
// 이메일 본문에서 CID를 찾아서, 실제 이미지 경로로 변환합니다.
    private String parseEmailContent(String emailHtml, Map<String, String> cidMap) {
        log.info("CID 대체 작업 시작...");

        for (Map.Entry<String, String> entry : cidMap.entrySet()) {
            String cid = entry.getKey();
            String imagePath = entry.getValue();
            log.info("CID: {}를 이미지 경로: {}로 대체 시도 중...", cid, imagePath);

            // CID를 실제 경로로 변환
            emailHtml = emailHtml.replace("cid:" + cid, imagePath);
        }

        log.info("최종 이메일 본문: {}", emailHtml);
        return emailHtml;
    }


    public List<MessageDTO> listSentMessages(String userId) throws IOException {
        Gmail service = getInstance();
        log.info("Fetching sent emails for user: {}", userId);

        List<Message> messages = null;

        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("SENT"))
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch sent messages for user: {}: {}", userId, e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                try {
                    Message fullMessage = service.users().messages().get(userId, message.getId()).execute();
                    MessagePart payload = fullMessage.getPayload();

                    if (payload != null) {
                        List<MessagePartHeader> headers = payload.getHeaders();

                        MessageDTO messageDTO = new MessageDTO();
                        messageDTO.setId(message.getId());
                        messageDTO.setFrom(getHeader(headers, "From").orElse("Unknown"));
                        messageDTO.setTo(getHeader(headers, "To").orElse("Unknown"));
                        messageDTO.setSubject(getHeader(headers, "Subject").orElse("No Subject"));
                        messageDTO.setDate(getHeader(headers, "Date").orElse("Unknown Date"));

                        boolean isStarred = fullMessage.getLabelIds() != null && fullMessage.getLabelIds().contains("STARRED");
                        messageDTO.setStarred(isStarred);

                        messageDTOList.add(messageDTO);
                    } else {
                        log.warn("Payload is null for message ID: {}", message.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process message ID: {}: {}", message.getId(), e.getMessage());
                    // 특정 메시지를 처리하지 못했을 경우, 해당 메시지를 건너뜀
                }
            }
        } else {
            log.info("No sent emails found for user: {}", userId);
        }

        return messageDTOList;
    }


    //휴지통 라벨로 이동시키는 메서드
    public void moveToTrash(String userId, String messageId) throws IOException {
        Gmail service = getInstance();
        ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(List.of("INBOX")).setAddLabelIds(List.of("TRASH"));
        service.users().messages().modify(userId, messageId, mods).execute();
    }

    //휴지통 라벨에서 기본(inbox)라벨로 복구 시키는 메서드
    public void restoreMessages(String userId, List<String> messageIds) throws IOException {
        Gmail service = getInstance();
        ModifyMessageRequest mods = new ModifyMessageRequest()
                .setRemoveLabelIds(List.of("TRASH"))
                .setAddLabelIds(List.of("INBOX")); // 필요한 기본 라벨을 추가

        for (String messageId : messageIds) {
            service.users().messages().modify(userId, messageId, mods).execute();
        }
    }

    //휴지통 라벨의 리스트를 보여주는 메서드
    public List<MessageDTO> listTrashMessages(String userId) throws IOException {
        Gmail service = getInstance();

        List<Message> messages = null;
        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("TRASH"))
                    .setFields("messages(id,labelIds,payload(headers))") // 라벨 정보도 함께 가져옴
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch trash messages: {}", e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        if (messages == null) {
            log.warn("No messages found in trash.");
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();
        for (Message message : messages) {
            Message fullMessage = service.users().messages().get(userId, message.getId())
                    .setFields("id,labelIds,payload(headers)")
                    .execute();

            MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);

            boolean isUnread = fullMessage.getLabelIds() != null && fullMessage.getLabelIds().contains("UNREAD");
            messageDTO.setRead(!isUnread);

            messageDTOList.add(messageDTO);
        }
        return messageDTOList;
    }


    public void deleteMessagePermanently(String userId, String messageId) throws IOException {
        Gmail service = getInstance();
        log.info("Permanently deleting message with ID: {}", messageId);

        try {
            // 메시지 ID가 올바르게 전달되도록 수정
            service.users().messages().delete(userId, messageId).execute();
            log.info("Message permanently deleted successfully with ID: {}", messageId);
        } catch (IOException e) {
            log.error("Failed to permanently delete message with ID: {}", messageId, e);
            throw e;
        }
    }


    public String saveDraft(String recipientEmail, String subject, String messageText, List<String> filePaths) throws Exception {
        Gmail service = getInstance();
        log.info("Saving draft for recipient: {}, subject: {}", recipientEmail, subject);
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage email = new MimeMessage(session);

            email.setFrom(new InternetAddress("your-email@gmail.com"));
            email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipientEmail));
            email.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(messageText, "text/html; charset=UTF-8");
            multipart.addBodyPart(textPart);

            if (filePaths != null && !filePaths.isEmpty()) {
                for (String filePath : filePaths) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(filePath);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(Paths.get(filePath).getFileName().toString());
                    multipart.addBodyPart(attachmentPart);
                }
            }

            email.setContent(multipart);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            String encodedEmail = Base64.encodeBase64URLSafeString(buffer.toByteArray());

            Message message = new Message();
            message.setRaw(encodedEmail);

            Draft draft = new Draft();
            draft.setMessage(message);

            Draft createdDraft = service.users().drafts().create("me", draft).execute();
            log.info("Draft saved successfully with ID: {}", createdDraft.getId());

            return createdDraft.getId();
        } catch (MessagingException e) {
            log.error("Failed to create email message for draft: {}", e.getMessage());
            throw new Exception("Failed to create email message for draft", e);
        } catch (Exception e) {
            log.error("Failed to save draft: {}", e.getMessage());
            throw new Exception("Failed to save draft", e);
        }
    }

    public List<MessageDTO> listDrafts(String userId) throws IOException {
        Gmail service = getInstance();
        log.info("Fetching drafts for user: {}", userId);

        ListDraftsResponse response = null;
        List<Draft> drafts = null;

        try {
            response = service.users().drafts().list(userId).execute();
            drafts = response.getDrafts();
        } catch (Exception e) {
            log.error("Failed to fetch drafts for user: {}: {}", userId, e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        if (drafts == null || drafts.isEmpty()) {
            log.info("No drafts found for user: {}", userId);
        } else {
            log.info("Number of drafts found: {}", drafts.size());

            for (Draft draft : drafts) {
                try {
                    String messageId = draft.getMessage().getId();
                    Message message = service.users().messages().get(userId, messageId).execute();
                    MessagePart payload = message.getPayload();

                    if (payload != null) {
                        List<MessagePartHeader> headers = payload.getHeaders();

                        MessageDTO messageDTO = new MessageDTO();
                        messageDTO.setId(message.getId());
                        messageDTO.setDraftId(draft.getId());
                        messageDTO.setFrom(getHeader(headers, "From").orElse("Unknown"));
                        messageDTO.setTo(getHeader(headers, "To").orElse("Unknown"));
                        messageDTO.setSubject(getHeader(headers, "Subject").orElse("No Subject"));
                        messageDTO.setDate(getHeader(headers, "Date").orElse("Unknown Date"));

                        boolean isStarred = message.getLabelIds() != null && message.getLabelIds().contains("STARRED");
                        messageDTO.setStarred(isStarred);

                        messageDTOList.add(messageDTO);
                    } else {
                        log.warn("Payload is null for message ID: {}", messageId);
                    }
                } catch (Exception e) {
                    log.error("Failed to process draft with ID: {}: {}", draft.getId(), e.getMessage());
                    // 특정 드래프트를 처리하지 못했을 경우, 해당 드래프트를 건너뜀
                }
            }
        }

        return messageDTOList;
    }


    public void deleteDraft(String userId, String draftId) throws IOException {
        Gmail service = getInstance();
        log.info("Deleting draft with ID: {}", draftId);
        try {
            service.users().drafts().delete(userId, draftId).execute();
            log.info("Draft deleted successfully with ID: {}", draftId);
        } catch (IOException e) {
            log.error("Failed to delete draft with ID: {}", draftId, e);
            throw e;
        }
    }

    private void extractAttachments(MessagePart part, List<String> attachments, Set<String> inlineImagesCIDs) {
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String contentDisposition = subPart.getHeaders().stream()
                        .filter(header -> "Content-Disposition".equalsIgnoreCase(header.getName()))
                        .map(MessagePartHeader::getValue)
                        .findFirst()
                        .orElse(null);

                String contentId = subPart.getHeaders().stream()
                        .filter(header -> "Content-ID".equalsIgnoreCase(header.getName()))
                        .map(MessagePartHeader::getValue)
                        .findFirst()
                        .orElse(null);

                log.info("Processing part: Content-Disposition = {}, Content-ID = {}", contentDisposition, contentId);
                log.info("Part MIME Type: {}", subPart.getMimeType());
                log.info("Part Filename: {}", subPart.getFilename());

                if (contentId != null && (contentDisposition == null || contentDisposition.toLowerCase().contains("inline"))) {
                    inlineImagesCIDs.add(contentId.trim().replaceAll("<|>", ""));
                    log.info("Identified inline image with CID: {}", contentId);
                } else if (contentDisposition != null && contentDisposition.toLowerCase().contains("attachment")) {
                    attachments.add("C:/upload/" + subPart.getFilename());
                    log.info("Attachment identified and added: {}", subPart.getFilename());
                } else if (subPart.getMimeType().startsWith("multipart/")) {
                    extractAttachments(subPart, attachments, inlineImagesCIDs);
                }
            }
        }
    }

    public MessageDTO getDraftById(String userId, String draftId) throws IOException {
        Gmail service = getInstance();
        log.info(">> Fetching draft details for draft ID: {}", draftId);

        Draft draft = service.users().drafts().get(userId, draftId).execute();
        Message message = draft.getMessage();
        log.info(">> Draft fetched successfully. Message ID: {}", message.getId());

        MessagePart payload = message.getPayload();
        List<MessagePartHeader> headers = payload.getHeaders();

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setDraftId(draftId);
        messageDTO.setFrom(getHeader(headers, "From").orElse("Unknown"));
        messageDTO.setTo(getHeader(headers, "To").orElse("Unknown"));
        messageDTO.setSubject(getHeader(headers, "Subject").orElse("No Subject"));
        messageDTO.setDate(getHeader(headers, "Date").orElse("Unknown Date"));
        log.info(">> Draft headers processed. From: {}, To: {}, Subject: {}", messageDTO.getFrom(), messageDTO.getTo(), messageDTO.getSubject());

        Set<String> inlineImagesCIDs = new HashSet<>();
        List<String> attachments = new ArrayList<>();
        extractAttachments(payload, attachments, inlineImagesCIDs);
        messageDTO.setAttachments(attachments);
        log.info(">> Attachments and inline images extracted. Attachments count: {}, Inline Images count: {}", attachments.size(), inlineImagesCIDs.size());

        String bodyContent = getMessageContent(userId, message.getId());
        Map<String, String> cidMap = extractCidMap(payload);
        bodyContent = parseEmailContent(bodyContent, cidMap);
        messageDTO.setBody(bodyContent);

        if (bodyContent.length() > 1000) {
            log.info(">> Email body content processed. Content is too large to display in logs.");
        } else {
            log.info(">> Email body content processed: {}", bodyContent);
        }

        log.info(">> Email body content processed and CID mapped successfully.");

        log.info(">> Draft details retrieved: ID = {}, Subject = {}", messageDTO.getId(), messageDTO.getSubject());
        return messageDTO;
    }

    public List<MessageDTO> listStarredMessages(String userId) throws IOException {
        Gmail service = getInstance();
        log.info("Fetching starred emails for user: {}", userId);

        List<Message> messages = null;

        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("STARRED"))
                    .setFields("messages(id,labelIds,payload(headers))") // 라벨 정보도 함께 가져옴
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch starred messages: {}", e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                try {
                    Message fullMessage = service.users().messages().get(userId, message.getId())
                            .setFields("id,labelIds,payload(headers)")
                            .execute();

                    if (fullMessage != null) {
                        MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);

                        // 읽음/안읽음 상태 설정
                        boolean isUnread = fullMessage.getLabelIds() != null && fullMessage.getLabelIds().contains("UNREAD");
                        messageDTO.setRead(!isUnread);

                        messageDTOList.add(messageDTO);
                    } else {
                        log.warn("Full message is null for message ID: {}", message.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to fetch full message for message ID: {}: {}", message.getId(), e.getMessage());
                    // 특정 메시지를 가져오지 못했을 경우, 해당 메시지를 건너뜀
                }
            }
        } else {
            log.info("No starred emails found for user: {}", userId);
        }

        return messageDTOList;
    }


    public List<MessageDTO> listImportantMessages(String userId) throws IOException {
        Gmail service = getInstance();
        List<Message> messages = null;

        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("IMPORTANT"))
                    .setFields("messages(id,labelIds,payload(headers))") // 라벨 정보도 함께 가져옴
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch important messages: {}", e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        if (messages == null || messages.isEmpty()) {
            log.info("No important messages found for user: {}", userId);
            // 빈 리스트 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        for (Message message : messages) {
            Message fullMessage = service.users().messages().get(userId, message.getId())
                    .setFields("id,labelIds,payload(headers)")
                    .execute();

            MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);

            // 읽음/안읽음 상태 설정
            boolean isUnread = fullMessage.getLabelIds() != null && fullMessage.getLabelIds().contains("UNREAD");
            messageDTO.setRead(!isUnread);

            messageDTOList.add(messageDTO);
        }

        return messageDTOList;
    }


    public void addStar(String userId, String messageId) throws IOException {
        Gmail service = getInstance();
        ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(List.of("STARRED"));
        service.users().messages().modify(userId, messageId, mods).execute();
    }

    public void removeStar(String userId, String messageId) throws IOException {
        Gmail service = getInstance();
        ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(List.of("STARRED"));
        service.users().messages().modify(userId, messageId, mods).execute();
    }

    // 이메일 읽음 상태로 변경
    public void markAsRead(String userId, String messageId) throws IOException {
        modifyLabel(userId, messageId, List.of("UNREAD"), null);  // UNREAD 라벨 제거
    }

    // 이메일 읽지 않음 상태로 변경
    public void markAsUnread(String userId, String messageId) throws IOException {
        modifyLabel(userId, messageId, null, List.of("UNREAD"));  // UNREAD 라벨 추가
    }

    // Message를 받아 MessageDTO로 변환하는 메서드 추가
    public MessageDTO getMessageDTO(String userId, String messageId) throws IOException {
        Message message = getMessage(userId, messageId);
        return MessageDTO.fromMessage(message);
    }

    // 라벨을 생성하는 메서드
    public String createLabel(String userId, String labelName) throws IOException {
        Gmail service = getInstance();
        Label label = new Label()
                .setName(labelName)
                .setLabelListVisibility("labelShow")
                .setMessageListVisibility("show");

        Label createdLabel = service.users().labels().create(userId, label).execute();
        return createdLabel.getId();
    }

    // 라벨을 추가하거나 제거하는 메서드
    private void modifyLabel(String userId, String messageId, List<String> labelsToRemove, List<String> labelsToAdd) throws IOException {
        Gmail service = getInstance();
        ModifyMessageRequest mods = new ModifyMessageRequest()
                .setRemoveLabelIds(labelsToRemove)
                .setAddLabelIds(labelsToAdd);
        service.users().messages().modify(userId, messageId, mods).execute();
    }

    // 라벨 추가 메서드
    public void addLabelToMessage(String userId, String messageId, String labelName) throws IOException {
        String labelId = getOrCreateLabelId(userId, labelName);
        modifyLabel(userId, messageId, null, List.of(labelId));
    }

    // 라벨 제거 메서드
    public void removeLabelFromMessage(String userId, String messageId, String labelName) throws IOException {
        String labelId = getOrCreateLabelId(userId, labelName);
        modifyLabel(userId, messageId, List.of(labelId), null);
    }

    // 공통적으로 메일을 DTO로 변환하는 메서드
    private List<MessageDTO> convertMessagesToDTOs(List<Message> messages, String userId) throws IOException {
        List<MessageDTO> messageDTOList = new ArrayList<>();
        for (Message message : messages) {
            Message fullMessage = service.users().messages().get(userId, message.getId())
                    .setFields("id,payload(headers),labelIds")  // 라벨 정보도 함께 가져옴
                    .execute();

            MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);
            messageDTOList.add(messageDTO);
        }
        return messageDTOList;
    }

    // 라벨 ID를 가져오거나 없으면 생성하는 메서드
    public String getOrCreateLabelId(String userId, String labelName) throws IOException {
        Gmail service = getInstance();
        ListLabelsResponse labelsResponse = service.users().labels().list(userId).execute();
        for (Label label : labelsResponse.getLabels()) {
            if (label.getName().equalsIgnoreCase(labelName)) {
                return label.getId();
            }
        }
        return createLabel(userId, labelName);
    }

    public boolean isMessageImportant(String userId, String messageId) throws IOException {
        Gmail service = getInstance();
        Message message = service.users().messages().get(userId, messageId).execute();
        return message.getLabelIds() != null && message.getLabelIds().contains("IMPORTANT");
    }

    public List<MessageDTO> listInboxMessages(String userId) throws IOException {
        Gmail service = getInstance();
        log.info("Fetching inbox emails for user: {}", userId);

        List<Message> messages = null;

        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("INBOX"))
                    .setFields("messages(id,labelIds,payload(headers))") // 필요한 필드만 가져옴
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch inbox messages for user: {}: {}", userId, e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                try {
                    // fullMessage를 가져와서 필요한 필드를 사용
                    Message fullMessage = service.users().messages().get(userId, message.getId())
                            .setFields("id,labelIds,payload(headers)")
                            .execute();

                    if (fullMessage != null) {
                        MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);
                        messageDTOList.add(messageDTO);
                    } else {
                        log.warn("Full message is null for message ID: {}", message.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process message ID: {}: {}", message.getId(), e.getMessage());
                    // 특정 메시지를 처리하지 못했을 경우, 해당 메시지를 건너뜀
                }
            }
        } else {
            log.info("No inbox emails found for user: {}", userId);
        }

        return messageDTOList;
    }


    public String createMyselfLabel(String userId) throws IOException {
        Gmail service = getInstance();
        String labelName = "내게 쓴 메일함";

        ListLabelsResponse labelsResponse = service.users().labels().list(userId).execute();
        for (Label label : labelsResponse.getLabels()) {
            if (label.getName().equalsIgnoreCase(labelName)) {
                return label.getId();
            }
        }

        return createLabel(userId, labelName);
    }

    public List<MessageDTO> listMyselfMessages(String userId) throws IOException {
        Gmail service = getInstance();
        log.info("Fetching emails sent to myself for user: {}", userId);

        List<Message> messages = null;

        try {
            messages = service.users().messages().list(userId)
                    .setLabelIds(Collections.singletonList("SENT"))
                    .setFields("messages(id,labelIds,payload(headers))") // 라벨 정보도 함께 가져옴
                    .execute()
                    .getMessages();
        } catch (Exception e) {
            log.error("Failed to fetch messages sent to myself: {}", e.getMessage());
            // 오류가 발생하면 로그를 남기고 빈 리스트를 반환
            return new ArrayList<>();
        }

        List<MessageDTO> messageDTOList = new ArrayList<>();

        if (messages != null && !messages.isEmpty()) {
            for (Message message : messages) {
                try {
                    Message fullMessage = service.users().messages().get(userId, message.getId())
                            .setFields("id,labelIds,payload(headers)")
                            .execute();

                    if (fullMessage != null) {
                        MessagePart payload = fullMessage.getPayload();

                        if (payload != null) {
                            List<MessagePartHeader> headers = payload.getHeaders();
                            String from = getHeader(headers, "From").orElse("");
                            String to = getHeader(headers, "To").orElse("");

                            if (from.equalsIgnoreCase(to)) {
                                String labelId = getOrCreateLabelId(userId, "내게 쓴 메일함");

                                MessageDTO messageDTO = MessageDTO.fromMessage(fullMessage);

                                // 읽음/안읽음 상태 설정
                                boolean isUnread = fullMessage.getLabelIds() != null && fullMessage.getLabelIds().contains("UNREAD");
                                messageDTO.setRead(!isUnread);

                                messageDTOList.add(messageDTO);

                                if (fullMessage.getLabelIds() == null || !fullMessage.getLabelIds().contains(labelId)) {
                                    addLabelToMessage(userId, message.getId(), "내게 쓴 메일함");
                                }
                            }
                        } else {
                            log.warn("Payload is null for message ID: {}", message.getId());
                        }
                    } else {
                        log.warn("Full message is null for message ID: {}", message.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to process message ID: {}: {}", message.getId(), e.getMessage());
                    // 특정 메시지를 처리하지 못했을 경우, 해당 메시지를 건너뜀
                }
            }
        } else {
            log.info("No messages found with 'SENT' label.");
        }

        log.info("Number of messages processed: {}", messageDTOList.size());
        return messageDTOList;
    }


}