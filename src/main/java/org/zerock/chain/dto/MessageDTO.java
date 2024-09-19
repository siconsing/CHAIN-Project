package org.zerock.chain.dto;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;
import java.util.Optional;

/*
* Getter:
필드의 값을 반환하는 메서드.
클래스 외부에서 해당 필드의 값을 읽어야 할 때 사용.
예시)getId()는 id 필드의 값을 반환.
*
Setter:
필드의 값을 설정(수정)하는 메서드.
클래스 외부에서 해당 필드의 값을 변경해야 할 때 사용.
예시) setId(String id)는 id 필드에 새로운 값을 설정함.
*/

public class MessageDTO {
    // 이메일의 ID (메시지 고유 식별자)
    private String id;

    // 드래프트(임시 저장된 메시지)의 ID
    private String draftId;

    // 발신자 이메일 주소
    private String from;

    // 수신자 이메일 주소
    private String to;

    // 이메일 제목
    private String subject;

    // 이메일 발신 날짜
    private String date;

    // 이메일이 중요 표시(별표 표시)가 되어 있는지 여부
    private boolean starred;

    // 이메일 본문 내용
    private String body;

    // 첨부파일 리스트
    private List<String> attachments;

    // 이메일의 읽음 상태를 나타내는 필드
    private boolean isRead;

    // id 필드의 Getter
    public String getId() {
        return id;
    }

    // id 필드의 Setter
    public void setId(String id) {
        this.id = id;
    }

    // draftId 필드의 Getter
    public String getDraftId() {
        return draftId;
    }

    // draftId 필드의 Setter
    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    // from 필드의 Getter
    public String getFrom() {
        return from;
    }

    // from 필드의 Setter
    public void setFrom(String from) {
        this.from = from;
    }

    // to 필드의 Getter
    public String getTo() {
        return to;
    }

    // to 필드의 Setter
    public void setTo(String to) {
        this.to = to;
    }

    // subject 필드의 Getter
    public String getSubject() {
        return subject;
    }

    // subject 필드의 Setter
    public void setSubject(String subject) {
        this.subject = subject;
    }

    // date 필드의 Getter
    public String getDate() {
        return date;
    }

    // date 필드의 Setter
    public void setDate(String date) {
        this.date = date;
    }

    // starred 필드의 Getter
    public boolean isStarred() {
        return starred;
    }

    // starred 필드의 Setter
    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    // body 필드의 Getter
    public String getBody() {
        return body;
    }

    // body 필드의 Setter
    public void setBody(String body) {
        this.body = body;
    }

    // attachments 필드의 Getter
    public List<String> getAttachments() {
        return attachments;
    }

    // attachments 필드의 Setter
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    // isRead 필드의 Getter
    public boolean isRead() {
        return isRead;
    }

    // isRead 필드의 Setter
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Gmail API의 Message 객체를 MessageDTO로 변환하는 메서드
    public static MessageDTO fromMessage(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());  // 메시지 ID 설정

        // 메시지의 페이로드(본문 및 헤더 정보)
        MessagePart payload = message.getPayload();
        if (payload != null) {
            List<MessagePartHeader> headers = payload.getHeaders();
            // 'From' 헤더의 값을 가져와서 설정, 없으면 'Unknown' 설정
            dto.setFrom(getHeader(headers, "From").orElse("Unknown"));
            // 'To' 헤더의 값을 가져와서 설정, 없으면 'Unknown' 설정
            dto.setTo(getHeader(headers, "To").orElse("Unknown"));
            // 'Subject' 헤더의 값을 가져와서 설정, 없으면 'No Subject' 설정
            dto.setSubject(getHeader(headers, "Subject").orElse("No Subject"));
            // 'Date' 헤더의 값을 가져와서 설정, 없으면 'Unknown Date' 설정
            dto.setDate(getHeader(headers, "Date").orElse("Unknown Date"));

            // 필요 시 본문(body) 및 첨부파일(attachments) 처리 로직 추가 가능
        }

        // 메시지가 'STARRED' 라벨을 포함하고 있으면 starred 필드를 true로 설정
        dto.setStarred(message.getLabelIds() != null && message.getLabelIds().contains("STARRED"));
        // 'UNREAD' 라벨이 없으면 읽음 상태로 설정
        dto.setRead(!message.getLabelIds().contains("UNREAD"));
        return dto;
    }

    // MessageDTO를 다시 Gmail API의 Message 객체로 변환하는 메서드
    public Message toMessage() {
        Message message = new Message();
        message.setId(this.getId());  // 메시지 ID 설정
        // 필요한 경우 더 많은 필드를 추가하여 Message 객체 구성 가능
        return message;
    }

    // 메시지 헤더에서 지정된 이름의 값을 Optional로 반환하는 유틸리티 메서드
    private static Optional<String> getHeader(List<MessagePartHeader> headers, String name) {
        return headers.stream()
                .filter(header -> header.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst();
    }

    // 답장 본문을 생성하는 메서드
    public static String buildReplyBody(MessageDTO messageDTO) {
        return String.format(
                "<br><br>-----Original Message-----<br>" +
                        "From: %s<br>" +
                        "To: %s<br>" +
                        "Cc: %s<br>" +
                        "Sent: %s<br>" +
                        "Subject: %s<br><br>%s",
                messageDTO.getFrom(),
                messageDTO.getTo(),
                "", // Cc는 공백으로 남김 (필요 시 수정 가능)
                messageDTO.getDate(),
                messageDTO.getSubject(),
                messageDTO.getBody()
        );
    }

    // 전달 본문을 생성하는 메서드
    public static String buildForwardBody(MessageDTO messageDTO) {
        return String.format(
                "<br><br>-----Original Message-----<br>" +
                        "From: %s<br>" +
                        "To: %s<br>" +
                        "Cc: %s<br>" +
                        "Sent: %s<br>" +
                        "Subject: %s<br><br>%s",
                messageDTO.getFrom(),
                messageDTO.getTo(),
                "", // Cc는 공백으로 남김 (필요 시 수정 가능)
                messageDTO.getDate(),
                messageDTO.getSubject(),
                messageDTO.getBody()
        );
    }

}
