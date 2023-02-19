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
import net.forlornly.automation.model.Sensor;
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

        Sensor s;

        switch (command.getOperation()) {
            case "setSensor":
                persistenceService.setSensor(command.getDataSensor().getMRId(), command.getDataSensor().getValue(),
                        command.getDataSensor().getTimestamp(), true);

                s = persistenceService.getSensor(command.getDataSensor().getMRId());

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(s)));
                break;

            case "getSensor":
                s = persistenceService.getSensor(command.getDataSensor().getMRId());

                if (s != null && s.isReported()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(s)));
                }
                break;

            case "register":
                s = persistenceService.getSensor(command.getDataSensor().getMRId());

                if (s == null) {
                    persistenceService.setSensor(command.getDataSensor().getMRId(), command.getDataSensor().getValue(),
                            command.getDataSensor().getTimestamp(), false);
                } else {
                    if (s.isReported()) {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(s)));
                    }
                }

                persistenceService.addWebSocketSessionToSensor(command.getDataSensor().getMRId(), session);
                break;

            default:
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        persistenceService.removeAllWebSocketSessionFromSensors(session);
    }
}
