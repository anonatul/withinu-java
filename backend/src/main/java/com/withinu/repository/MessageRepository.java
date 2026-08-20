package com.withinu.repository;

import com.withinu.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByRoomIdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);

    Page<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = """
        SELECT r.id, r.name, r.slug, r.description,
               COUNT(m.id) AS message_count,
               MAX(m.created_at) AS last_activity
        FROM rooms r
        LEFT JOIN messages m ON m.room_id = r.id AND m.deleted = false
        WHERE r.active = true
        GROUP BY r.id
        ORDER BY r.name
        """, nativeQuery = true)
    List<Object[]> findActiveRoomsWithStats();

    long countByDeletedTrue();

    long countByDeletedFalse();

    @Query("SELECT COUNT(DISTINCT m.anonymousUser.id) FROM Message m WHERE m.deleted = false")
    long countDistinctActiveUsers();
}