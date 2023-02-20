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
import net.forlornly.automation.model.Equipment;
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

            log.info("Loaded persistence, retrieved {} value(s)", root.getEquipmentList().size());
        }
    }

    public synchronized void setEquipment(String mRId, int value, Timestamp timestamp, boolean reported)
            throws IOException {
        for (Equipment e : root.getEquipmentList()) {
            if (e.getMRId().equals(mRId)) {
                e.setValue(value);
                e.setTimestamp(timestamp);
                e.setTimestamp(timestamp);
                e.setReported(reported);

                storageManager.store(e);

                log.debug("Updated Equipment {}", e);

                if (e.isReported()) {
                    log.debug("Sending Equipment {} to {} WebSocketSession(s)",
                            e, e.getWebSocketSessions().size());

                    for (WebSocketSession wss : e.getWebSocketSessions()) {
                        wss.sendMessage(new TextMessage(objectMapper.writeValueAsString(e)));
                    }
                }

                return;
            }
        }

        root.getEquipmentList().add(new Equipment(mRId, value, timestamp, reported, new ArrayList<WebSocketSession>()));

        storageManager.store(root.getEquipmentList());

        log.debug("Created Equipment {} with value {}, timestamp {} and reported {}", mRId, value, timestamp, reported);
    }

    public synchronized Equipment getEquipment(String mRId) {
        for (Equipment e : root.getEquipmentList()) {
            if (e.getMRId().equals(mRId)) {
                log.debug("Found Equipment {}", e);

                return e;
            }
        }

        log.debug("Equipment {} not found", mRId);

        return null;
    }

    public synchronized void addWebSocketSessionToEquipment(String mRId, WebSocketSession session) {
        for (Equipment e : root.getEquipmentList()) {
            if (e.getMRId().equals(mRId)) {
                for (WebSocketSession wss : e.getWebSocketSessions()) {
                    if (wss.equals(session)) {
                        log.debug("Ignoring adding WebSocketSession {} to Equipment {}, duplicate registration",
                                session,
                                e.getMRId());

                        return;
                    }
                }

                e.getWebSocketSessions().add(session);

                log.debug("Added WebSocketSession {} to Equipment {}", session, e.getMRId());

                return;
            }
        }

        log.debug("Equipment {} not found", mRId);
    }

    public synchronized void removeAllWebSocketSessionFromEquipmentList(WebSocketSession session) {
        log.debug("Removing WebSocketSession {} from EquipmentList", session);

        for (Equipment e : root.getEquipmentList()) {
            e.getWebSocketSessions().removeIf(wss -> wss.equals(session));
        }
    }
}
