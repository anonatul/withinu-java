package com.withinu.repository;

import com.withinu.entity.AnonymousUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnonymousUserRepository extends JpaRepository<AnonymousUser, UUID> {
}