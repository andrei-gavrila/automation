package net.forlornly.automation.persistence;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.forlornly.automation.model.Sensor;

@Getter
@Setter
public class Root {
    private List<Sensor> sensors = new ArrayList<Sensor>();
}
