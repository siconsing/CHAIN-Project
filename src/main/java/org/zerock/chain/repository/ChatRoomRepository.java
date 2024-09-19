package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.ChatRoom;
import org.zerock.chain.model.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 삭제되지 않은 방을 조회하도록 수정
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.senderEmpNo = :senderEmpNo AND cr.recipientEmpNo = :recipientEmpNo AND cr.isDeleted = false")
    Optional<ChatRoom> findBySenderEmpNoAndRecipientEmpNo(@Param("senderEmpNo") Long senderEmpNo, @Param("recipientEmpNo") Long recipientEmpNo);

    // 방이 소프트 삭제되었는지 확인
    @Query("SELECT cr.isDeleted FROM ChatRoom cr WHERE cr.senderEmpNo = :senderEmpNo AND cr.recipientEmpNo = :recipientEmpNo")
    boolean isDeleted(@Param("senderEmpNo") Long senderEmpNo, @Param("recipientEmpNo") Long recipientEmpNo);

    // 삭제되지 않은 방 존재 여부 확인
    @Query("SELECT COUNT(cr) > 0 FROM ChatRoom cr WHERE cr.senderEmpNo = :senderEmpNo AND cr.recipientEmpNo = :recipientEmpNo AND cr.isDeleted = false")
    boolean existsBySenderEmpNoAndRecipientEmpNo(@Param("senderEmpNo") Long senderEmpNo, @Param("recipientEmpNo") Long recipientEmpNo);

    // 방이 소프트 삭제되었는지 확인
    @Query("SELECT cr.isDeleted FROM ChatRoom cr WHERE cr.chatNo = :chatNo")
    List<Boolean> findIsDeletedByChatNo(@Param("chatNo") String chatNo);

    // chatNo에 해당하는 모든 방을 삭제
    @Modifying
    @Query("DELETE FROM ChatRoom cr WHERE cr.chatNo = :chatNo")
    void deleteByChatNo(@Param("chatNo") String chatNo);

    // 사용자와 대화 중인 다른 사용자 목록
    @Query("SELECT u FROM Employee u WHERE u.empNo IN (SELECT cr.recipientEmpNo FROM ChatRoom cr WHERE cr.senderEmpNo = :empNo AND cr.isDeleted = false)")
    List<Employee> findActiveChatUsersByEmpNo(@Param("empNo") Long empNo);

    // 동일한 chatNo에서 isDeleted값이 false 경우만 최신시간 업데이트
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.chatNo = :chatNo AND cr.isDeleted = false")
    List<ChatRoom> findByChatNoAndIsDeletedFalse(@Param("chatNo") String chatNo);
}