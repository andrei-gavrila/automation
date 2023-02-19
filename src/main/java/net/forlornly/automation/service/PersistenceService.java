package net.forlornly.automation.service;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import lombok.extern.log4j.Log4j2;
import net.forlornly.automation.model.Sensor;
import net.forlornly.automation.persistence.Root;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

@Service
@Log4j2
public class PersistenceService {
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

    public synchronized void setSensor(String mRId, int value, Timestamp timestamp) {
        for (Sensor s : root.getSensors()) {
            if (s.getMRId().equals(mRId)) {
                s.setValue(value);
                s.setTimestamp(timestamp);

                storageManager.store(s);

                log.debug("Updated sensor {} with value {} and timestamp {}", mRId, value, timestamp);

                return;
            }
        }

        root.getSensors().add(new Sensor(mRId, value, timestamp, new ArrayList<WebSocketSession>()));

        storageManager.store(root.getSensors());

        log.debug("Created sensor {} with value {} and timestamp {}", mRId, value, timestamp);
    }

    public synchronized Sensor getSensor(String mRId) {
        for (Sensor s : root.getSensors()) {
            if (s.getMRId().equals(mRId)) {
                log.debug("Found sensor {} with value {} and timestamp {}", s.getMRId(), s.getValue(),
                        s.getTimestamp());

                return s;
            }
        }

        log.debug("Sensor {} not found", mRId);

        return null;
    }
}
