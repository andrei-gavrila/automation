package net.forlornly.automation.model;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Sensor {
    private String mRId;
    private int value;
    private Timestamp timestamp;
    private boolean reported;
    @JsonIgnore
    private List<WebSocketSession> webSocketSessions;
}
