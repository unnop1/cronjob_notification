package com.nt.cronjob_notification.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nt.cronjob_notification.entity.NotificationMsgEntity;

public interface NotificationMessageRepo extends JpaRepository<NotificationMsgEntity,Long> {
    
    @Query(value = "SELECT * FROM notification_message ", nativeQuery = true)
    public List<NotificationMsgEntity> ListNotificationMsg(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM notification_message ", nativeQuery = true)
    public Integer getTotalCount();
    
}
