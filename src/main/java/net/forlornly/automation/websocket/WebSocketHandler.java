package net.forlornly.automation.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.forlornly.automation.dto.Command;
import net.forlornly.automation.model.Equipment;
import net.forlornly.automation.model.User;
import net.forlornly.automation.service.PersistenceService;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersistenceService persistenceService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Command command = objectMapper.readValue(message.getPayload(), Command.class);

        User u;
        Equipment e;

        switch (command.getOperation()) {
            case "login":
                u = persistenceService.getUser(command.getLogin().getUsername(), command.getLogin().getPassword());

                if (u != null) {
                    persistenceService.setSession(u, session);
                }
                break;

            case "set":
                u = persistenceService.getUserFromSession(session);

                if (u != null && u.getRoles().contains("writer")) {
                    persistenceService.setEquipment(command.getEquipment().getMRId(), command.getEquipment().getValue(),
                            command.getEquipment().getTimestamp(), true);

                    e = persistenceService.getEquipment(command.getEquipment().getMRId());

                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(e)));
                }
                break;

            case "get":
                u = persistenceService.getUserFromSession(session);

                if (u != null && u.getRoles().contains("reader")) {
                    e = persistenceService.getEquipment(command.getEquipment().getMRId());

                    if (e != null && e.isReported()) {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(e)));
                    }
                }
                break;

            case "register":
                u = persistenceService.getUserFromSession(session);

                if (u != null && u.getRoles().contains("reader")) {
                    e = persistenceService.getEquipment(command.getEquipment().getMRId());

                    if (e == null) {
                        persistenceService.setEquipment(command.getEquipment().getMRId(),
                                command.getEquipment().getValue(),
                                command.getEquipment().getTimestamp(), false);
                    } else {
                        if (e.isReported()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(e)));
                        }
                    }

                    persistenceService.addWebSocketSessionToEquipment(command.getEquipment().getMRId(), session);
                }
                break;

            default:
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        persistenceService.removeSession(session);

        persistenceService.removeAllWebSocketSessionFromEquipmentList(session);
    }
}
