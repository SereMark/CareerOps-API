package com.seregergo.careerops.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationStatusEventRepository extends JpaRepository<ApplicationStatusEvent, UUID> {
}
