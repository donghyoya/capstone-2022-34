package com.capstone.momomeal.api;

import com.capstone.momomeal.domain.*;
import com.capstone.momomeal.service.ChatRoomService;
import com.capstone.momomeal.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatRoomListApiController {
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    /**
     * 사용자가 참여한 채팅방 제외한 해당 카테고리별 채팅방 데이터(dto) 전송 api
     * @param categoryName 사용자가 선택한 카테고리명
     * @return 해당 카테고리에 해당하는 모든 채팅방 dto 리스트 Body에 담은 ResponseEntity
     */
    @GetMapping("/chat-list/{categoryName}/{testMemberId}")
    public ResponseEntity returnCategoryList(@PathVariable String categoryName,
                                             @PathVariable Long testMemberId){
        // string -> Category enum 타입 변환
        TransStringToEnum te = new TransStringToEnum();
        Category selectedCategory = te.transferStringToEnum(categoryName);


        // 참여한 채팅 제외한 모든 채팅방
        List<ChatRoom> chatRooms = getChatRoomsExceptParticipatedCharRooms(testMemberId);


        // 해당 카테고리의 dto만 뽑음
        List<ChatRoomListDto> result = chatRooms.stream()
                                        .filter(c -> c.getCategory().equals(selectedCategory))
                                        .map(c -> new ChatRoomListDto(c))
                                        .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);

    }




    /**
     * 사용자가 참여한 채팅방 제외한 모든 채팅방 데이터(dto) 전송 api
     * @return 모든 채팅방의 dto 리스트 Body에 담은 ResponseEntity
     */
    @GetMapping("/chat-list/{testMemberId}")
    public ResponseEntity returnAllList(@PathVariable Long testMemberId) {

        // 모든 채팅방 가져옴
        // 참여한 채팅 제외한 모든 채팅방
        List<ChatRoom> chatRooms = getChatRoomsExceptParticipatedCharRooms(testMemberId);

        // 모든 채팅방의 dto만 뽑음
        List<ChatRoomListDto> result = chatRooms.stream().map(c -> new ChatRoomListDto(c))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);

    }

    // 참여한 채팅 제외한 모든 채팅방 리턴
    private List<ChatRoom> getChatRoomsExceptParticipatedCharRooms(Long testMemberId) {

        // 사용자가 이미 참여한 채팅 거르기 위해 사용자가 참여한 채팅방 id(ChatRoomId)값이 필요
        Member member = memberService.findOne(testMemberId);
        List<JoinedChatRoom> joinedChatRooms = member.getJoinedChatRooms(); // 참여한 joinedChatRooms
        List<Long> participatedChatRoomIds = new ArrayList<>();    // 참여한 chatRoomIds

        for (JoinedChatRoom joinedChatRoom : joinedChatRooms) {
            Long participatedChatRoomId = joinedChatRoom.getChatRoom().getId();
            participatedChatRoomIds.add(participatedChatRoomId);
        }   // end

        List<ChatRoom> chatRooms = new ArrayList<>();

        // 참여하고 있는 채팅방이 없을 때 -> 전체 채팅방 가져옴
        if (participatedChatRoomIds.size() < 1) {
            chatRooms = chatRoomService.findAll();
        } else{     // 참여하고 있는 채팅방이 있을 때
            // 참여하고 있는 채팅방 제외한 모든 채팅방 가져옴
            chatRooms = chatRoomService.findExceptParticipatedChatRoom(participatedChatRoomIds);
        }


        return chatRooms;
    }


    // response body가 []가 아닌 {}로 감싸기 위한 장치
    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class ChatRoomListDto{
        private Long id;
        private String title;
        private String pickupPlaceName;
        private LocalDateTime createdDate;
        private double pickupPlaceXCoord;
        private double pickupPlaceYCoord;

        public ChatRoomListDto(ChatRoom chatRoom) {
            this.id = chatRoom.getId();
            this.title = chatRoom.getTitle();
            this.pickupPlaceName = chatRoom.getPickupPlaceName();
            this.createdDate = chatRoom.getCreatedDate();
            this.pickupPlaceXCoord = chatRoom.getPickupPlaceXCoord();
            this.pickupPlaceYCoord = chatRoom.getPickupPlaceYCoord();
        }
    }

    



}