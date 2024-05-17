package com.nt.cronjob_notification.model.distribute.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TriggerOrderTypeCountResp {
    @JsonProperty("statusCode")
    private int statusCode=200;

    @JsonProperty("count")
    private int count;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<OrderTypeTriggerData> data;

    @JsonProperty("draw")
    private Integer draw=11;

    @JsonProperty("recordsTotal")
    private Integer recordsTotal;

    @JsonProperty("recordsFiltered")
    private Integer recordsFiltered;
    
}