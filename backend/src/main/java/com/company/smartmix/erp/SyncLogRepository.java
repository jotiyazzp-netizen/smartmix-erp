package com.company.smartmix.erp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 同步日志Repository
 */
@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {
}
