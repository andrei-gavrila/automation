package net.forlornly.automation.persistence;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.forlornly.automation.model.Equipment;
import net.forlornly.automation.model.Session;
import net.forlornly.automation.model.User;

@Getter
@Setter
public class Root {
    private List<Equipment> equipmentList = new ArrayList<Equipment>();
    private List<User> userList = new ArrayList<User>();
    private List<Session> sessionList = new ArrayList<Session>();
}
