package com.seregergo.careerops.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStatusEventRepository extends JpaRepository<ApplicationStatusEvent, UUID> {

	List<ApplicationStatusEvent> findAllByApplicationIdOrderByOccurredAtAscIdAsc(UUID applicationId);
}
