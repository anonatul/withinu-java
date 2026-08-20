package com.withinu.repository;

import com.withinu.entity.Report;
import com.withinu.moderation.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    List<Report> findTop10ByOrderByCreatedAtDesc();

    long countByStatus(ReportStatus status);
}