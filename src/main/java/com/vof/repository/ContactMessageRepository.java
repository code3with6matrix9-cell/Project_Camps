package com.vof.repository;
import com.vof.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {}
