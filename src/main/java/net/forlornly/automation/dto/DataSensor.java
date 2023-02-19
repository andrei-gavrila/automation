package net.forlornly.automation.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataSensor {
    @JsonProperty("mRId")
    private String mRId;
    private int value;
    private Timestamp timestamp;
}
