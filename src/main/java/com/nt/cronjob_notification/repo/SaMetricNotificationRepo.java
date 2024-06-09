package com.nt.cronjob_notification.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nt.cronjob_notification.entity.SaMetricNotificationEntity;

public interface SaMetricNotificationRepo extends JpaRepository<SaMetricNotificationEntity,Long> {
    @Query(value = "SELECT * FROM sa_metric_notification ", nativeQuery = true)
    public List<SaMetricNotificationEntity> ListSaMetrics();

    @Query(value = "SELECT COUNT(*) FROM sa_metric_notification ", nativeQuery = true)
    public Integer getTotalCount();
}
