package net.forlornly.automation.model;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Equipment {
    private String mRId;
    private int value;
    private Timestamp timestamp;
    private boolean reported;
    @JsonIgnore
    @ToString.Exclude
    private List<WebSocketSession> webSocketSessions;
}
