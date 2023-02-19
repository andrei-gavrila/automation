package net.forlornly.automation.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import net.forlornly.automation.model.Sensor;
import net.forlornly.automation.persistence.Root;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

@Service
@Log4j2
public class PersistenceService {
    @Autowired
    private ObjectMapper objectMapper;

    private final EmbeddedStorageManager storageManager;
    private Root root = new Root();

    public PersistenceService(@Value("${persistence.path}") String path) {
        log.info("Starting persistence, path: {}", path);

        storageManager = EmbeddedStorage.start(Paths.get(path));

        if (storageManager.root() == null) {
            storageManager.setRoot(root);

            storageManager.storeRoot();

            log.info("Created new persistence");
        } else {
            root = (Root) storageManager.root();

            log.info("Loaded persistence, retrieved {} value(s)", root.getSensors().size());
        }
    }

    public synchronized void setSensor(String mRId, int value, Timestamp timestamp, boolean reported)
            throws IOException {
        for (Sensor s : root.getSensors()) {
            if (s.getMRId().equals(mRId)) {
                s.setValue(value);
                s.setTimestamp(timestamp);
                s.setTimestamp(timestamp);
                s.setReported(reported);

                storageManager.store(s);

                log.debug("Updated sensor {} with value {}, timestamp {} and reported {}", mRId, value, timestamp,
                        reported);

                if (s.isReported()) {
                    log.debug("Sending sensor {} with value {}, timestamp {} and reported {} to {} WebSocketSession(s)",
                            mRId, value, timestamp, reported, s.getWebSocketSessions().size());

                    for (WebSocketSession wss : s.getWebSocketSessions()) {
                        wss.sendMessage(new TextMessage(objectMapper.writeValueAsString(s)));
                    }
                }
                return;
            }
        }

        root.getSensors().add(new Sensor(mRId, value, timestamp, reported, new ArrayList<WebSocketSession>()));

        storageManager.store(root.getSensors());

        log.debug("Created sensor {} with value {}, timestamp {} and reported {}", mRId, value, timestamp, reported);
    }

    public synchronized Sensor getSensor(String mRId) {
        for (Sensor s : root.getSensors()) {
            if (s.getMRId().equals(mRId)) {
                log.debug("Found sensor {} with value {}, timestamp {} and reported {}", s.getMRId(), s.getValue(),
                        s.getTimestamp(), s.isReported());

                return s;
            }
        }

        log.debug("Sensor {} not found", mRId);

        return null;
    }

    public synchronized void addWebSocketSessionToSensor(String mRId, WebSocketSession session) {
        for (Sensor s : root.getSensors()) {
            if (s.getMRId().equals(mRId)) {
                for (WebSocketSession wss : s.getWebSocketSessions()) {
                    if (wss.equals(session)) {
                        log.debug("Ignoring adding WebSocketSession {} to sensor {}, duplicate registration", session,
                                s.getMRId());

                        return;
                    }
                }

                s.getWebSocketSessions().add(session);

                log.debug("Added WebSocketSession {} to sensor {}", session, s.getMRId());

                return;
            }
        }

        log.debug("Sensor {} not found", mRId);
    }

    public synchronized void removeAllWebSocketSessionFromSensors(WebSocketSession session) {
        log.debug("Removing WebSocketSession {} from all sensors", session);

        for (Sensor s : root.getSensors()) {
            s.getWebSocketSessions().removeIf(wss -> wss.equals(session));
        }
    }
}
