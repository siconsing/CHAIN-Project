package org.zerock.chain.controller;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.chain.dto.MessageDTO;
import org.zerock.chain.service.GmailService;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.Path;
import java.util.*;

import org.apache.commons.codec.binary.Base64; // 이걸 임포트


import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.Gmail;

@Log4j2
@Controller
@RequestMapping("/mail")   // "/mail" 경로 하위에 있는 요청들을 처리.
public class EmailController {

    @Autowired  // GmailService를 자동으로 주입받음.
    private GmailService gmailService;

    private static final String UPLOAD_DIR = "C:/upload/";

    @GetMapping("/threads")
    public String listThreads(@RequestParam("accessToken") String accessToken, Model model) {
        try {
            // Gmail 서비스 객체를 초기화 (Access Token을 사용)
            Gmail service = new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken))
                    .setApplicationName("Your App Name")
                    .build();

            // Gmail API를 사용하여 스레드 목록을 가져옴
            ListThreadsResponse threadsResponse = service.users().threads().list("me").execute();

            // 스레드 목록을 모델에 추가하여 뷰로 전달
            model.addAttribute("threads", threadsResponse.getThreads());

            // 스레드 목록을 표시할 페이지로 이동
            return "threads"; // 스레드를 보여줄 view 파일 (threads.html)로 이동
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // 오류가 발생했을 때 에러 페이지로 이동
        }
    }


    // 메일 작성 페이지를 반환하는 메서드
    @GetMapping("/compose")
    public String mailCompose() {
        return "mail/compose";
    }

    @PostMapping("/compose")
    public String composeEmail(@RequestParam("subject") String subject,
                               @RequestParam("messageContent") String messageContent,
                               @RequestParam("recipientEmail") String recipientEmail,
                               Model model) {
        model.addAttribute("subject", subject);
        model.addAttribute("message", messageContent);
        model.addAttribute("recipientEmail", recipientEmail);

        // 필요한 다른 로직을 여기에 추가하세요 (예: 첨부 파일 처리 등)

        return "mail/compose";  // compose.html 페이지로 이동
    }

    // 메일을 전송하는 메서드
    @PostMapping("/send")
    public String sendEmail(
            @RequestParam("recipientEmail") String recipientEmail,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments,
            @RequestParam(value = "existingAttachments", required = false) List<String> existingAttachments,
            @RequestParam(value = "deleteAttachments", required = false) List<String> deleteAttachments,
            @RequestParam(value = "draftId", required = false) String draftId,
            Model model) {
        log.info("sendEmail called with recipient: {}, subject: {}", recipientEmail, subject);
        try {
            // 첨부파일 저장 경로 리스트 생성
            List<String> filePaths = new ArrayList<>();

            // 새로운 첨부파일 처리
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        String fileName = StringUtils.cleanPath(attachment.getOriginalFilename());
                        Path path = Paths.get(UPLOAD_DIR, fileName).normalize();

                        // 파일을 저장하고 경로를 filePaths 리스트에 추가
                        Files.write(path, attachment.getBytes());
                        filePaths.add(path.toString());  // 절대 경로로 저장
                        log.info("Attachment saved: name = {}, path = {}, size = {} bytes",
                                fileName, path.toString(), attachment.getSize());
                    }
                }
            }

            // 이메일 주소를 콤마로 분리하여 리스트로 변환
            String[] recipients = recipientEmail.split(",");

            // 이메일 전송
            for (String recipient : recipients) {
                gmailService.sendMail(recipient.trim(), subject, message, filePaths);
            }

            model.addAttribute("success", "Email sent successfully to all recipients!");
        } catch (Exception e) {
            log.error("Error sending email", e);
            model.addAttribute("error", "Error sending email: " + e.getMessage());
            return "mail/compose";
        }
        return "mail/complete";
    }


    // 메일 전송 완료 페이지를 반환하는 메서드
    @GetMapping("/complete")
    public String mailComplete() {
        return "mail/complete";
    }


    // 경로를 처리하는 메서드
    private Path resolveFilePath(String fileName) {
        Path path = Paths.get(fileName).normalize();
        if (!path.isAbsolute()) {
            path = Paths.get(UPLOAD_DIR, fileName).normalize();
        }
        return path;
    }

    // 이미지를 업로드하는 메서드
    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadImage(@RequestParam("image") String imageData) {
        try {
            String base64Image = imageData.split(",")[1];
            byte[] imageBytes = Base64.decodeBase64(base64Image);
            String fileName = UUID.randomUUID().toString() + ".png";
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, imageBytes);
            log.info("Image uploaded successfully: {}", path.toString());
            return ResponseEntity.ok(Map.of("filePath", "/upload/" + fileName));
        } catch (IOException e) {
            log.error("Image upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }

    // 임시저장된 데이터를 반환하는 메서드
    @GetMapping("/drafts/edit/{draftId}")
    public String editDraft(@PathVariable("draftId") String draftId, Model model) {
        log.info("editDraft called with draftId: {}", draftId);
        try {
            MessageDTO draftMessage = gmailService.getDraftById("me", draftId);
            log.info("Draft message fetched: {}", draftMessage);

            model.addAttribute("draftId", draftId);
            model.addAttribute("recipientEmail", draftMessage.getTo());
            model.addAttribute("subject", draftMessage.getSubject());
            model.addAttribute("message", draftMessage.getBody());

            List<String> attachments = draftMessage.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                log.info("Attachments found: {}", attachments);
                model.addAttribute("attachments", attachments);
            } else {
                log.info("No attachments found or attachments list is empty.");
                model.addAttribute("attachments", List.of());
            }

            return "mail/compose";
        } catch (IOException e) {
            log.error("Error fetching draft for editing", e);
            model.addAttribute("error", "Error fetching draft: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/forward")
    public String forwardEmail(@RequestParam("subject") String subject,
                               @RequestParam("messageContent") String messageContent,
                               @RequestParam("recipientEmail") String recipientEmail,
                               @RequestParam(value = "attachments", required = false) List<String> attachments,
                               Model model) {
        model.addAttribute("subject", subject);

        // 본문이 HTML 형식임을 보장
        model.addAttribute("message", "<html>" + messageContent + "</html>");
        model.addAttribute("recipientEmail", recipientEmail);

        // 첨부파일 정보 추가
        if (attachments != null && !attachments.isEmpty()) {
            model.addAttribute("attachments", attachments);
        }

        return "mail/compose"; // compose.html 페이지로 이동
    }


    @PostMapping("/reply")
    public String replyEmail(@RequestParam("subject") String subject,
                             @RequestParam("messageContent") String messageContent,
                             @RequestParam("recipientEmail") String recipientEmail,
                             @RequestParam(value = "attachments", required = false) List<String> attachments,
                             Model model) {
        model.addAttribute("subject", subject);

        // 본문이 HTML 형식임을 보장
        model.addAttribute("message", "<html>" + messageContent + "</html>");
        model.addAttribute("recipientEmail", recipientEmail);

        // 첨부파일 정보 추가
        if (attachments != null && !attachments.isEmpty()) {
            model.addAttribute("attachments", attachments);
        }

        return "mail/compose"; // compose.html 페이지로 이동
    }


    // 메일 전체 수신함 INBOX 메일함을 표시하는 메서드 추가 [메일의 메인 페이지]
    @GetMapping("/inbox")
    public String listInboxEmails(Model model) {
        log.info("listInboxEmails called");
        try {
            // GmailService에서 INBOX 라벨이 적용된 이메일 목록을 가져옴
            List<MessageDTO> inboxMessages = gmailService.listInboxMessages("me");
            log.info("inbox------------------->{}", inboxMessages);
            model.addAttribute("messages", inboxMessages);
            model.addAttribute("success", "Inbox emails fetched successfully!");
        } catch (IOException e) {
            log.error("Error fetching inbox emails", e);
            model.addAttribute("error", "Error fetching inbox emails: " + e.getMessage());
        }
        return "mail/inbox";  // 받은 메일함 뷰로 반환
    }


    // 이메일을 읽고 본문(상세 내용)을 표시하는 메서드
    @GetMapping("/view")
    public String viewEmail(@RequestParam("messageId") String messageId,
                            @RequestParam(value = "returnUrl", required = false) String returnUrl,
                            Model model, HttpServletRequest request) {
        log.info("viewEmail called with messageId: {}", messageId);
        try {
            // 이메일을 가져옴
            Message message = gmailService.getMessage("me", messageId);

            // 읽은 상태로 변경
            gmailService.markAsRead("me", messageId);

            List<MessagePartHeader> headers = message.getPayload().getHeaders();
            String subject = gmailService.getHeader(headers, "Subject").orElse("No Subject");

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setId(messageId);
            messageDTO.setSubject(subject);
            messageDTO.setFrom(gmailService.getHeader(headers, "From").orElse("Unknown"));
            messageDTO.setTo(gmailService.getHeader(headers, "To").orElse("Unknown Recipient"));
            messageDTO.setDate(gmailService.getHeader(headers, "Date").orElse("Unknown Date"));

            String messageContent = gmailService.getMessageContent("me", messageId);

            // CID 기반 이미지를 실제 경로로 변경
            Pattern pattern = Pattern.compile("cid:([\\w\\-\\.]+)@\\w+\\.\\w+");
            Matcher matcher = pattern.matcher(messageContent);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String cid = matcher.group(1).replaceAll("[^a-zA-Z0-9]", "_");
                String imagePath = "/assets/img/mailimg/image_" + cid + ".jpeg";
                matcher.appendReplacement(sb, imagePath);
            }
            matcher.appendTail(sb);

            messageContent = sb.toString();

            // 이메일이 별표 표시되었는지 확인
            boolean isStarred = message.getLabelIds() != null && message.getLabelIds().contains("STARRED");
            messageDTO.setStarred(isStarred);

            // 이메일이 휴지통(TRASH) 라벨이 있는지 확인
            boolean isInTrash = message.getLabelIds() != null && message.getLabelIds().contains("TRASH");

            // 이메일이 발신함(SENT) 라벨에 있는지 확인
            boolean isInSent = message.getLabelIds() != null && message.getLabelIds().contains("SENT");

            log.info("Final message content: {}", messageContent);

            model.addAttribute("message", messageDTO);
            model.addAttribute("messageContent", messageContent);
            model.addAttribute("messageId", messageId);
            model.addAttribute("isInTrash", isInTrash); // 휴지통 여부를 모델에 추가
            model.addAttribute("isInSent", isInSent); // 발신함 여부를 모델에 추가

            // returnUrl 처리
            if (returnUrl != null && !returnUrl.isEmpty()) {
                log.info("returnUrl provided: {}", returnUrl);
                model.addAttribute("returnUrl", returnUrl);
            } else {
                // Referer 헤더를 사용하거나 기본적으로 수신 메일함으로 설정
                String referer = request.getHeader("Referer");
                if (referer != null && !referer.isEmpty()) {
                    log.info("Referer found: {}", referer);
                    model.addAttribute("returnUrl", referer);
                } else {
                    log.info("Referer not found, setting default returnUrl to /mail/inbox");
                    model.addAttribute("returnUrl", "/mail/inbox");
                }
            }

        } catch (IOException e) {
            log.error("Error fetching email", e);
            model.addAttribute("error", "Error fetching email: " + e.getMessage());
            return "error";
        }
        return "mail/mailRead";
    }


    // 이메일 읽음 상태를 토글하거나 특정 상태로 설정하는 메서드
    @PostMapping("/toggleReadStatus")
    public ResponseEntity<String> toggleReadStatus(
            @RequestBody Map<String, Object> payload) {
        String messageId = (String) payload.get("messageId");

        if (messageId == null || messageId.isEmpty()) {
            log.error("Message ID is missing in the request");
            return ResponseEntity.badRequest().body("Message ID is required");
        }

        Boolean markAsRead = (Boolean) payload.get("markAsRead");

        log.info("Toggle read status for messageId: {} to {}", messageId, markAsRead);

        try {
            if (markAsRead != null) {
                if (markAsRead) {
                    gmailService.markAsRead("me", messageId);
                    log.info("Marked as read for messageId: {}", messageId);
                } else {
                    gmailService.markAsUnread("me", messageId);
                    log.info("Marked as unread for messageId: {}", messageId);
                }
            } else {
                MessageDTO message = gmailService.getMessageDTO("me", messageId);
                if (message.isRead()) {
                    gmailService.markAsUnread("me", messageId);
                    log.info("Toggled to unread for messageId: {}", messageId);
                } else {
                    gmailService.markAsRead("me", messageId);
                    log.info("Toggled to read for messageId: {}", messageId);
                }
            }
            return ResponseEntity.ok("Success");
        } catch (IOException e) {
            log.error("Failed to toggle read status for messageId: {}", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to toggle read status");
        }
    }


    // 보낸 메일함을 표시하는 메서드 추가
    @GetMapping("/sent")
    public String listSentEmails(Model model) {
        log.info("listSentEmails called");
        try {
            // GmailService에서 SENT 라벨이 적용된 이메일 목록을 가져옴
            List<MessageDTO> sentMessages = gmailService.listSentMessages("me");
            model.addAttribute("messages", sentMessages);
            model.addAttribute("success", "Sent emails fetched successfully!");
        } catch (IOException e) {
            log.error("Error fetching sent emails", e);
            model.addAttribute("error", "Error fetching sent emails: " + e.getMessage());
        }
        return "mail/sent";  // 보낸 메일함 뷰로 반환
    }


    // 메시지를 휴지통으로 이동시키는 메서드 추가
    @PostMapping("/trash/{messageId}")
    public String moveToTrash(@PathVariable String messageId, Model model) {
        try {
            gmailService.moveToTrash("me", messageId);
            model.addAttribute("success", "Email moved to trash successfully!");
        } catch (IOException e) {
            model.addAttribute("error", "Failed to move email to trash: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/mail/trash"; // 휴지통 페이지로 리다이렉트
    }

    // 휴지통으로 이동하는 요청 처리 메서드
    @PostMapping("/moveToTrash")
    public ResponseEntity<String> moveToTrash(@RequestBody List<String> messageIds) {
        try {
            for (String messageId : messageIds) {
                gmailService.addLabelToMessage("me", messageId, "TRASH");
            }
            return ResponseEntity.ok("Messages moved to trash successfully");
        } catch (IOException e) {
            log.error("Error moving messages to trash", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to move messages to trash");
        }
    }

    // 휴지통에서 메일 복구 요청 처리 메서드
    @PostMapping("/trash/restoreSelected")
    public ResponseEntity<String> restoreSelectedMessages(@RequestBody List<String> messageIds) {
        try {
            gmailService.restoreMessages("me", messageIds);
            return ResponseEntity.ok("Messages restored successfully");
        } catch (IOException e) {
            log.error("Error restoring messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to restore messages");
        }
    }


    // 휴지통에 있는 메시지 목록을 보여주는 메서드
    @GetMapping("/trash")
    public String listTrashEmails(Model model) {
        try {
            List<MessageDTO> messages = gmailService.listTrashMessages("me");
            model.addAttribute("messages", messages);
        } catch (IOException e) {
            model.addAttribute("error", "Failed to retrieve trash emails: " + e.getMessage());
        }
        return "mail/trash";
    }

    // 개별 메시지를 영구 삭제하는 메서드 추가
    @PostMapping("/trash/delete/{messageId}")
    public String deleteMessagePermanently(@PathVariable String messageId, RedirectAttributes redirectAttributes) {
        log.debug("deleteMessagePermanently invoked with messageId: {}", messageId);

        if (messageId == null || messageId.isEmpty()) {
            log.error("Invalid messageId: {}", messageId);
            redirectAttributes.addFlashAttribute("error", "Invalid message ID.");
            return "redirect:/mail/trash";
        }

        try {
            log.info("Attempting to delete message with ID: {}", messageId);
            gmailService.deleteMessagePermanently("me", messageId);
            redirectAttributes.addFlashAttribute("success", "Message permanently deleted successfully!");
            log.info("Message with ID: {} deleted successfully", messageId);
        } catch (IOException e) {
            log.error("IOException while deleting message with ID: {}. Error: {}", messageId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error permanently deleting message: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while deleting message with ID: {}. Error: {}", messageId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred: " + e.getMessage());
        }

        return "redirect:/mail/trash";  // Redirect to the trash page
    }


    // 선택된 메시지를 영구 삭제하는 메서드 추가
    @PostMapping("/trash/deleteSelected")
    public String deleteSelectedMessagesPermanently(@RequestParam("messageIds") List<String> messageIds, RedirectAttributes redirectAttributes) {
        log.info("deleteSelectedMessagesPermanently called with messageIds: {}", messageIds);
        try {
            for (String messageId : messageIds) {
                gmailService.deleteMessagePermanently("me", messageId);
            }
            redirectAttributes.addFlashAttribute("success", "Selected messages permanently deleted successfully!");
        } catch (IOException e) {
            log.error("Error permanently deleting selected messages", e);
            redirectAttributes.addFlashAttribute("error", "Error permanently deleting selected messages: " + e.getMessage());
        }
        return "redirect:/mail/trash";  // 휴지통 페이지로 리다이렉트
    }

    // 휴지통에 있는 모든 이메일을 영구 삭제하는 메서드 추가
    @PostMapping("/trash/deleteAll")
    public String deleteAllMessagesInTrash(RedirectAttributes redirectAttributes) {
        log.info("deleteAllMessagesInTrash called");

        try {
            List<MessageDTO> trashMessages = gmailService.listTrashMessages("me");
            if (trashMessages.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "휴지통이 이미 비어 있습니다.");
                return "redirect:/mail/trash";
            }

            for (MessageDTO message : trashMessages) {
                gmailService.deleteMessagePermanently("me", message.getId());
            }
            redirectAttributes.addFlashAttribute("success", "모든 메시지가 영구 삭제되었습니다.");
        } catch (IOException e) {
            log.error("모든 메시지 영구 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "모든 메시지 영구 삭제 실패: " + e.getMessage());
        }
        return "redirect:/mail/trash"; // 휴지통 페이지로 리다이렉트
    }


    // 작성 데이터를 임시저장(Draft) 시키는 메서드
    @PostMapping("/saveDraft")
    public String saveDraft(
            @RequestParam("recipientEmail") String recipientEmail,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments,
            Model model) {
        log.info("saveDraft called with recipient: {}, subject: {}", recipientEmail, subject);
        try {
            // 첨부파일 저장 경로 리스트
            List<String> filePaths = new ArrayList<>();
            if (attachments != null && !attachments.isEmpty()) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        String fileName = StringUtils.cleanPath(attachment.getOriginalFilename());
                        Path path = Paths.get(UPLOAD_DIR, fileName).normalize();

                        // 이미 파일 경로가 리스트에 존재하지 않는 경우에만 추가
                        if (!filePaths.contains(path.toString())) {
                            Files.write(path, attachment.getBytes());
                            filePaths.add(path.toString());  // 절대 경로로 저장
                            log.info("Attachment saved: name = {}, path = {}, size = {} bytes",
                                    fileName, path.toString(), attachment.getSize());
                        }
                    }
                }
            }

            // 새로운 초안을 생성하거나, 기존 초안을 업데이트
            String draftId = gmailService.saveDraft(recipientEmail, subject, message, filePaths);
            model.addAttribute("success", "Draft saved successfully with ID: " + draftId);
        } catch (Exception e) {
            log.error("Error saving draft", e);
            model.addAttribute("error", "Error saving draft: " + e.getMessage());
        }
        return "mail/compose";  // 임시저장 후에도 compose 페이지로 리다이렉트
    }


    // Draft 목록을 가져와 표시하는 메서드 추가
    @GetMapping("/draftsList")
    public String listDrafts(Model model) {
        log.info("listDrafts called");
        try {
            List<MessageDTO> drafts = gmailService.listDrafts("me");

            if (drafts == null || drafts.isEmpty()) {
                log.info("No drafts found");
            } else {
                log.info("Drafts found: {}", drafts.size());
            }

            model.addAttribute("messages", drafts);
            model.addAttribute("success", "Drafts fetched successfully!");
        } catch (IOException e) {
            log.error("Error fetching drafts", e);
            model.addAttribute("error", "Error fetching drafts: " + e.getMessage());
        }
        return "mail/draftsList";
    }

    // 임시보관함(Draft)에서 초안을 개별 삭제하는 메서드 추가
    @PostMapping("/drafts/delete/{draftId}")
    public String deleteDraft(@PathVariable String draftId, RedirectAttributes redirectAttributes) {
        log.info("deleteDraft called with draftId: {}", draftId);
        try {
            gmailService.deleteDraft("me", draftId);
            redirectAttributes.addFlashAttribute("success", "Draft deleted successfully!");
        } catch (IOException e) {
            log.error("Error deleting draft", e);
            redirectAttributes.addFlashAttribute("error", "Error deleting draft: " + e.getMessage());
        }
        return "redirect:/mail/draftsList";  // 초안 목록 페이지로 리다이렉트
    }

    //임시보관함(Draft)에서 선택 삭제를 위한 메서드
    @PostMapping("/drafts/deleteSelected")
    public String deleteSelectedDrafts(@RequestParam("draftIds") List<String> draftIds, RedirectAttributes redirectAttributes) {
        log.info("deleteSelectedDrafts called with draftIds: {}", draftIds);
        try {
            for (String draftId : draftIds) {
                gmailService.deleteDraft("me", draftId);
            }
            redirectAttributes.addFlashAttribute("success", "Selected drafts deleted successfully!");
        } catch (IOException e) {
            log.error("Error deleting drafts", e);
            redirectAttributes.addFlashAttribute("error", "Error deleting selected drafts: " + e.getMessage());
        }
        return "redirect:/mail/draftsList";  // 초안 목록 페이지로 리다이렉트
    }

    //별표 메일함(starred)을 표시하는 메서드 추가
    @GetMapping("/starred")
    public String listStarredEmails(Model model) {
        log.info("listStarredEmails called");
        try {
            // GmailService에서 STARRED 라벨이 적용된 이메일 목록을 가져옴
            List<MessageDTO> starredMessages = gmailService.listStarredMessages("me");
            model.addAttribute("messages", starredMessages);
            model.addAttribute("success", "Starred emails fetched successfully!");
        } catch (IOException e) {
            log.error("Error fetching starred emails", e);
            model.addAttribute("error", "Error fetching starred emails: " + e.getMessage());
        }
        return "mail/starred";
    }

    //별표(starred) 서버와 연동 토글 위한 메서드 추가
    @PostMapping("/toggleStar")
    public ResponseEntity<String> toggleStar(@RequestBody Map<String, Object> payload) {
        String messageId = (String) payload.get("messageId");
        boolean starred = (Boolean) payload.get("starred");

        log.info("Toggle star for messageId: {} to {}", messageId, starred);

        try {
            if (starred) {
                gmailService.addStar("me", messageId);
                log.info("Star added for messageId: {}", messageId);
            } else {
                gmailService.removeStar("me", messageId);
                log.info("Star removed for messageId: {}", messageId);
            }
            return ResponseEntity.ok("Success");
        } catch (IOException e) {
            log.error("Failed to update starred status for messageId: {}", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update star status");
        }
    }


    // 중요 메일함(IMPORTANT)을 표시하는 메서드 추가
    @GetMapping("/important")
    public String listImportantEmails(Model model) {
        log.info("listImportantEmails called");
        try {
            // GmailService에서 IMPORTANT 라벨이 적용된 이메일 목록을 가져옴
            List<MessageDTO> importantMessages = gmailService.listImportantMessages("me");
            model.addAttribute("messages", importantMessages);
            model.addAttribute("success", "Important emails fetched successfully!");
        } catch (IOException e) {
            log.error("Error fetching important emails", e);
            model.addAttribute("error", "Error fetching important emails: " + e.getMessage());
        }
        return "mail/important";  // 중요 메일함 뷰로 반환
    }


    //중요(IMPORTANT) 라벨 메서드로 이동하는 메서드
    @PostMapping("/markAsImportant")
    public ResponseEntity<Map<String, List<String>>> markAsImportant(@RequestBody List<String> messageIds) {
        Map<String, List<String>> results = new HashMap<>();
        List<String> alreadyImportantTitles = new ArrayList<>();
        List<String> markedAsImportantTitles = new ArrayList<>();

        try {
            for (String messageId : messageIds) {
                Message message = gmailService.getMessage("me", messageId);
                String subject = gmailService.getHeader(message.getPayload().getHeaders(), "Subject").orElse("No Subject");

                if (gmailService.isMessageImportant("me", messageId)) {
                    alreadyImportantTitles.add(subject);
                } else {
                    gmailService.addLabelToMessage("me", messageId, "IMPORTANT");
                    markedAsImportantTitles.add(subject);
                }
            }
            results.put("alreadyImportant", alreadyImportantTitles);
            results.put("markedAsImportant", markedAsImportantTitles);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            log.error("Error marking messages as important", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", List.of("Failed to mark messages as important")));
        }
    }

    // 중요하지 않음(IMPORTANT 라벨 제거)으로 이동하는 메서드
    @PostMapping("/unmarkAsImportant")
    public ResponseEntity<Map<String, List<String>>> unmarkAsImportant(@RequestBody List<String> messageIds) {
        Map<String, List<String>> results = new HashMap<>();
        List<String> unmarkedAsImportantTitles = new ArrayList<>();

        try {
            for (String messageId : messageIds) {
                Message message = gmailService.getMessage("me", messageId);
                String subject = gmailService.getHeader(message.getPayload().getHeaders(), "Subject").orElse("No Subject");

                if (gmailService.isMessageImportant("me", messageId)) {
                    gmailService.removeLabelFromMessage("me", messageId, "IMPORTANT");
                    unmarkedAsImportantTitles.add(subject);
                }
            }
            results.put("unmarkedAsImportant", unmarkedAsImportantTitles);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            log.error("Error unmarking messages as important", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", List.of("Failed to unmark messages as important")));
        }
    }


    // "내게 쓴 메일함"을 표시하는 메서드 추가
    @GetMapping("/myself")
    public String listMyselfEmails(Model model) {
        log.info("listMyselfEmails called");
        try {
            // GmailService에서 본인에게 보낸 메일 목록을 가져옴
            gmailService.createMyselfLabel("me"); // 먼저 라벨이 존재하는지 확인하고 없으면 생성

            // 여기에 myselfMessages 변수를 선언하고 초기화합니다.
            List<MessageDTO> myselfMessages = gmailService.listMyselfMessages("me");

            model.addAttribute("messages", myselfMessages);
            model.addAttribute("success", "Myself emails fetched successfully!");

            // 여기서 myselfMessages의 크기를 로그로 출력해보세요.
            log.info("Number of emails in '내게 쓴 메일함': {}", myselfMessages.size());

        } catch (IOException e) {
            log.error("Error fetching myself emails", e);
            model.addAttribute("error", "Error fetching myself emails: " + e.getMessage());
        }
        return "mail/myself";  // "내게 쓴 메일함" 뷰로 반환
    }

}