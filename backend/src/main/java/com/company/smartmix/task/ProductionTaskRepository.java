package com.company.smartmix.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 生产任务Repository
 */
@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {

    Optional<ProductionTask> findByTaskNo(String taskNo);

    boolean existsByTaskNo(String taskNo);
}
