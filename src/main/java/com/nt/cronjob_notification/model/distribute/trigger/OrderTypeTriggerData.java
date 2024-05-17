package com.nt.cronjob_notification.model.distribute.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderTypeTriggerData {
    /*
        "ordertype_NAME": "TESTCREATE",
        "totalTrigger": 0,
    */

    @JsonProperty("ordertype_NAME")
    private String ordertype_NAME;

    @JsonProperty("totalTrigger")
    private Integer totalTrigger;
}
