package com.RoutineGongJakSo.BE.chat.controller;


import com.RoutineGongJakSo.BE.chat.dto.ChatMessageDto;
import com.RoutineGongJakSo.BE.chat.dto.model.ChatMessage;
import com.RoutineGongJakSo.BE.chat.pubsub.RedisPublisher;
import com.RoutineGongJakSo.BE.chat.repo.ChatRoomRepository;
import com.RoutineGongJakSo.BE.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageService chatMessageService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        Date date = new Date();
        System.out.println("date = " + date);
        message.setCreatedAt(date.toString().substring(11,19));
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            chatRoomRepository.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        } else {
            chatMessageService.save(message);
        }
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        redisPublisher.publish(chatRoomRepository.getTopic(message.getRoomId()), new ChatMessageDto(message));
    }

    @GetMapping("/chat/message/{roomId}")
    @ResponseBody
    public List<ChatMessageDto> getMessages(@PathVariable String roomId) {
        return chatMessageService.getMessages(roomId);
    }
}
