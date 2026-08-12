package com.kolaysoft.projedurum.repository;

import com.kolaysoft.projedurum.entity.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {
    List<TaskItem> findByProjectId(Long projectId);
}
