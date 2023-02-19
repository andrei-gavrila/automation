package net.forlornly.automation.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import net.forlornly.automation.model.Sensor;

@Service
public class PersistenceService {
    private List<Sensor> sensors = new ArrayList<Sensor>();

    public synchronized void setSensor(String mRId, int value, Timestamp timestamp) {
        for (Sensor s : sensors) {
            if (s.getMRId().equals(mRId)) {
                s.setValue(value);
                s.setTimestamp(timestamp);

                return;
            }
        }

        sensors.add(new Sensor(mRId, value, timestamp, new ArrayList<WebSocketSession>()));
    }

    public synchronized Sensor getSensor(String mRId) {
        for (Sensor s : sensors) {
            if (s.getMRId().equals(mRId)) {
                return s;
            }
        }

        return null;
    }
}
