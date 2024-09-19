package org.zerock.chain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.chain.model.ChatRoom;
import org.zerock.chain.repository.ChatMessageRepository;
import org.zerock.chain.repository.ChatNotificationRepository;
import org.zerock.chain.repository.ChatRoomRepository;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    @Autowired
    private final ChatRoomRepository chatRoomRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatNotificationRepository chatNotificationRepository;

    // 단일 ChatRoom 객체를 반환하도록 수정
    public Optional<ChatRoom> getChatRoom(Long senderEmpNo, Long recipientEmpNo, boolean createNewRoomIfNotExists) {
        return chatRoomRepository
                .findBySenderEmpNoAndRecipientEmpNo(senderEmpNo, recipientEmpNo)
                .or(() -> {
                    if (createNewRoomIfNotExists) {
                        return Optional.of(createChatRoom(senderEmpNo, recipientEmpNo)); // 새로운 방 생성
                    }
                    return Optional.empty();
                });
    }

    // 안 읽은 메시지 카운트 초기화
    public void resetUnreadCount(Long senderEmpNo, Long recipientEmpNo) {
        var chatRoom = getChatRoom(senderEmpNo, recipientEmpNo, false);
        chatRoom.ifPresent(room -> {
            room.setUnreadCount(0); // unread_count 초기화
            chatRoomRepository.save(room); // DB에 저장
        });
    }

    // 채팅방을 생성하고, ChatRoom 객체를 생성하여 반환
    public Optional<String> getChatRoomNo(Long senderEmpNo, Long recipientEmpNo, boolean createNewRoomIfNotExists) {
        return getChatRoom(senderEmpNo, recipientEmpNo, createNewRoomIfNotExists).map(ChatRoom::getChatNo);
    }

    // 채팅방 생성: 기존 방이 존재하지 않는 경우에만 방을 추가
    private ChatRoom createChatRoom(Long senderEmpNo, Long recipientEmpNo) {
        // 작은 값이 앞에 오고 큰 값이 뒤에 오도록 정렬
        Long smallerEmpNo = Math.min(senderEmpNo, recipientEmpNo);
        Long largerEmpNo = Math.max(senderEmpNo, recipientEmpNo);

        // chatNo를 "작은값_큰값" 형식으로 생성
        var chatNo = String.format("%s_%s", smallerEmpNo, largerEmpNo);

        boolean senderRoomExists = chatRoomRepository.existsBySenderEmpNoAndRecipientEmpNo(smallerEmpNo, largerEmpNo);
        boolean recipientRoomExists = chatRoomRepository.existsBySenderEmpNoAndRecipientEmpNo(largerEmpNo, smallerEmpNo);

        if (senderRoomExists && recipientRoomExists) {
            throw new IllegalStateException("이미 존재하는 방입니다.");
        }

        ChatRoom newChatRoom = null;
        if (!senderRoomExists) {
            ChatRoom senderRecipient = ChatRoom.builder()
                    .chatNo(chatNo)
                    .senderEmpNo(smallerEmpNo)
                    .recipientEmpNo(largerEmpNo)
                    .isDeleted(false)
                    .build();
            chatRoomRepository.save(senderRecipient);
            newChatRoom = senderRecipient;
        }
        if (!recipientRoomExists) {
            ChatRoom recipientSender = ChatRoom.builder()
                    .chatNo(chatNo)
                    .senderEmpNo(largerEmpNo)
                    .recipientEmpNo(smallerEmpNo)
                    .isDeleted(false)
                    .build();
            chatRoomRepository.save(recipientSender);
            if (newChatRoom == null) {
                newChatRoom = recipientSender;
            }
        }
        return newChatRoom;
    }

    // 방 삭제
    @Transactional
    public void exitChatRoom(Long senderEmpNo, Long recipientEmpNo) {
        try {
            // 기존 채팅방을 조회합니다.
            Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findBySenderEmpNoAndRecipientEmpNo(senderEmpNo, recipientEmpNo);

            // 채팅방이 존재하면 is_deleted 값을 true로 변경합니다.
            chatRoomOpt.ifPresent(chatRoom -> {
                chatRoom.setDeleted(true);
                chatRoomRepository.save(chatRoom); // 변경된 엔티티를 저장합니다.
            });

            String chatNo = chatRoomOpt.map(ChatRoom::getChatNo).orElse(null);
            if (chatNo != null && isChatRoomCompletelyDeleted(chatNo)) {
                // chatNo에 해당하는 모든 데이터를 삭제합니다.
                chatNotificationRepository.deleteByChatNo(chatNo);
                chatMessageRepository.deleteByChatNo(chatNo);
                chatRoomRepository.deleteByChatNo(chatNo);
            }
        } catch (Exception e) {
            log.error("채팅방 삭제 중 오류 발생: ", e);
            throw new RuntimeException("채팅방을 삭제하는 동안 오류가 발생했습니다.");
        }
    }

    // 방이 완전히 삭제되었는지 확인하는 메서드
    public boolean isChatRoomCompletelyDeleted(String chatNo) {
        List<Boolean> isDeletedList = chatRoomRepository.findIsDeletedByChatNo(chatNo);
        // 모든 채팅방의 isDeleted가 true라면 true를 반환
        return isDeletedList.stream().allMatch(Boolean::booleanValue);
    }

    // 중복 방 존재 여부 확인 메서드
    public boolean checkRoomExistence(Long senderEmpNo, Long recipientEmpNo) {
        return chatRoomRepository.existsBySenderEmpNoAndRecipientEmpNo(senderEmpNo, recipientEmpNo);
    }

    // 방 삭제 확인
    public boolean isChatRoomDeleted(Long senderEmpNo, Long recipientEmpNo) {
        return chatRoomRepository.findBySenderEmpNoAndRecipientEmpNo(senderEmpNo, recipientEmpNo)
                .map(ChatRoom::isDeleted)
                .orElse(true);
    }

}
