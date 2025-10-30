package com.example.helpdeskticket.repository;

import com.example.helpdeskticket.model.Ticket;
import com.example.helpdeskticket.model.TicketStatus;

import com.example.helpdeskticket.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Derived: By user, ordered by createdAt desc (for user's ticket history)
    List<Ticket> findByUserOrderByCreatedAtDesc(User user);

    // Paginated by user ID (key for tracking, with sorting)
    Page<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // By status (e.g., all open tickets), sorted by createdAt asc
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);

    // Exists by title and user (prevent duplicate tickets per user)
    boolean existsByTitleAndUser(String title, User user);

    // Custom @Query: Count open tickets for a user (for dashboard/metrics)
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId AND t.status = :status")
    Long countTicketsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TicketStatus status);

    // Custom: Paginated tickets by status (e.g., agent's queue)
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    // All tickets with custom sort (use in service: e.g., Sort.by("status").ascending().and(Sort.by("resolvedAt").descending()))
    List<Ticket> findAll(Sort sort);

    // Example custom: Unresolved tickets (status != CLOSED), paginated
    // Use a derived query to find tickets with status != CLOSED and order by createdAt desc
    Page<Ticket> findByStatusNotOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);
}