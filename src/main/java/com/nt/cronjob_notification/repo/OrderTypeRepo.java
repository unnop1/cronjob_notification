package com.nt.cronjob_notification.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.nt.cronjob_notification.entity.OrderTypeEntity;
import com.nt.cronjob_notification.entity.view.trigger.TriggerOrderTypeCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepo extends JpaRepository<OrderTypeEntity,Long> {

    @Query(value = """
      SELECT 
        odt.*,
        (SELECT COUNT(id) FROM trigger_message trg WHERE trg.ordertype_id = odt.id AND TRUNC(trg.RECEIVE_DATE) = TRUNC(SYSDATE) ) AS TotalTrigger
      FROM 
        ordertype odt
      LEFT JOIN sa_channel_connect sac
      ON sac.id = odt.SA_CHANNEL_CONNECT_ID
        """,
      nativeQuery = true)
    public List<TriggerOrderTypeCount> AllOrderTypeTriggerCount(
    );

}
