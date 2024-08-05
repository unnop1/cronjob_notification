package com.nt.cronjob_notification.model.distribute.manage.metric;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriggerCheck {
    private List<String> messageList;
    private List<String> patternList;
}
