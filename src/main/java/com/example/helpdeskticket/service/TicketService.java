package com.example.helpdeskticket.service;

import com.example.helpdeskticket.model.Ticket;
import com.example.helpdeskticket.model.TicketStatus;
import com.example.helpdeskticket.model.User;
import com.example.helpdeskticket.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // Basic CRUD operations
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll(Sort sort) {
        return ticketRepository.findAll(sort);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    public Ticket createTicket(Ticket ticket) {
        // Validate required fields
        if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket title cannot be null or empty");
        }
        
        if (ticket.getUser() == null) {
            throw new IllegalArgumentException("Ticket must be associated with a user");
        }

        // Validate title length
        if (ticket.getTitle().length() > 1000) {
            throw new IllegalArgumentException("Ticket title cannot exceed 1000 characters");
        }

        // Validate description length
        if (ticket.getDescription() != null && ticket.getDescription().length() > 5000) {
            throw new IllegalArgumentException("Ticket description cannot exceed 5000 characters");
        }

        // Check for duplicate tickets (same title for same user)
        if (ticketRepository.existsByTitleAndUser(ticket.getTitle(), ticket.getUser())) {
            throw new IllegalArgumentException("Ticket with title '" + ticket.getTitle() + "' already exists for this user");
        }

        // Set default status if not provided
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        return ticketRepository.save(ticket);
    }

    public Ticket updateTicket(Long id, Ticket ticketDetails) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));

        // Validate title
        if (ticketDetails.getTitle() != null) {
            if (ticketDetails.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Ticket title cannot be empty");
            }
            if (ticketDetails.getTitle().length() > 1000) {
                throw new IllegalArgumentException("Ticket title cannot exceed 1000 characters");
            }
            existingTicket.setTitle(ticketDetails.getTitle());
        }

        // Validate description
        if (ticketDetails.getDescription() != null) {
            if (ticketDetails.getDescription().length() > 5000) {
                throw new IllegalArgumentException("Ticket description cannot exceed 5000 characters");
            }
            existingTicket.setDescription(ticketDetails.getDescription());
        }

        // Handle status changes
        if (ticketDetails.getStatus() != null) {
            updateTicketStatus(existingTicket, ticketDetails.getStatus());
        }

        return ticketRepository.save(existingTicket);
    }

    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
        
        ticketRepository.deleteById(id);
    }

    // Status management
    public Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + ticketId));
        
        return updateTicketStatus(ticket, newStatus);
    }

    private Ticket updateTicketStatus(Ticket ticket, TicketStatus newStatus) {
        TicketStatus oldStatus = ticket.getStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        // Set resolved timestamp when closing a ticket
        if ((newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) && 
            ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        // Clear resolved timestamp if reopening a resolved ticket
        if ((oldStatus == TicketStatus.RESOLVED || oldStatus == TicketStatus.CLOSED) && 
            (newStatus == TicketStatus.OPEN || newStatus == TicketStatus.IN_PROGRESS)) {
            ticket.setResolvedAt(null);
        }

        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);
    }

    private boolean isValidStatusTransition(TicketStatus from, TicketStatus to) {
        // Define valid status transitions
        return switch (from) {
            case OPEN -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.RESOLVED || to == TicketStatus.CLOSED;
            case IN_PROGRESS -> to == TicketStatus.OPEN || to == TicketStatus.RESOLVED || to == TicketStatus.CLOSED;
            case RESOLVED -> to == TicketStatus.CLOSED || to == TicketStatus.IN_PROGRESS;
            case CLOSED -> to == TicketStatus.OPEN; // Reopen closed tickets
        };
    }

    // User-specific operations
    @Transactional(readOnly = true)
    public List<Ticket> findByUserOrderByCreatedAtDesc(User user) {
        return ticketRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findByUserAndStatus(User user, TicketStatus status) {
        // This would need a custom repository method
        return ticketRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .filter(ticket -> ticket.getStatus() == status)
                .toList();
    }

    // Status-based operations
    @Transactional(readOnly = true)
    public List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status) {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ticket> findUnresolvedTickets(Pageable pageable) {
        return ticketRepository.findByStatusNotOrderByCreatedAtDesc(TicketStatus.CLOSED, pageable);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findOpenTickets() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.OPEN);
    }

    // Statistics and analytics
    @Transactional(readOnly = true)
    public Long countTicketsByUserIdAndStatus(Long userId, TicketStatus status) {
        return ticketRepository.countTicketsByUserIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public TicketStatistics getUserTicketStatistics(Long userId) {
        long openTickets = countTicketsByUserIdAndStatus(userId, TicketStatus.OPEN);
        long inProgressTickets = countTicketsByUserIdAndStatus(userId, TicketStatus.IN_PROGRESS);
        long resolvedTickets = countTicketsByUserIdAndStatus(userId, TicketStatus.RESOLVED);
        long closedTickets = countTicketsByUserIdAndStatus(userId, TicketStatus.CLOSED);
        long totalTickets = openTickets + inProgressTickets + resolvedTickets + closedTickets;

        return new TicketStatistics(openTickets, inProgressTickets, resolvedTickets, closedTickets, totalTickets);
    }

    @Transactional(readOnly = true)
    public TicketStatistics getOverallTicketStatistics() {
        List<Ticket> allTickets = ticketRepository.findAll();
        
        long openTickets = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgressTickets = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long resolvedTickets = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closedTickets = allTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();
        long totalTickets = allTickets.size();

        return new TicketStatistics(openTickets, inProgressTickets, resolvedTickets, closedTickets, totalTickets);
    }

    // Bulk operations
    public void closeResolvedTickets() {
        List<Ticket> resolvedTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.RESOLVED);
        for (Ticket ticket : resolvedTickets) {
            updateTicketStatus(ticket, TicketStatus.CLOSED);
        }
    }

    public void autoCloseOldResolvedTickets(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Ticket> oldResolvedTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.RESOLVED)
                .stream()
                .filter(ticket -> ticket.getResolvedAt() != null && ticket.getResolvedAt().isBefore(cutoffDate))
                .toList();
        
        for (Ticket ticket : oldResolvedTickets) {
            updateTicketStatus(ticket, TicketStatus.CLOSED);
        }
    }

    // Search and validation
    @Transactional(readOnly = true)
    public boolean existsByTitleAndUser(String title, User user) {
        return ticketRepository.existsByTitleAndUser(title, user);
    }

    @Transactional(readOnly = true)
    public boolean isTicketOwnedByUser(Long ticketId, Long userId) {
        return ticketRepository.findById(ticketId)
                .map(ticket -> ticket.getUser().getId().equals(userId))
                .orElse(false);
    }

    // Utility methods
    @Transactional(readOnly = true)
    public double getAverageResolutionTime() {
        List<Ticket> closedTickets = ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.CLOSED)
                .stream()
                .filter(ticket -> ticket.getResolvedAt() != null && ticket.getCreatedAt() != null)
                .toList();

        if (closedTickets.isEmpty()) {
            return 0.0;
        }

        double totalHours = closedTickets.stream()
                .mapToDouble(ticket -> 
                    java.time.Duration.between(ticket.getCreatedAt(), ticket.getResolvedAt()).toHours()
                )
                .sum();

        return totalHours / closedTickets.size();
    }

    // DTO for statistics
    public static class TicketStatistics {
        private final long openTickets;
        private final long inProgressTickets;
        private final long resolvedTickets;
        private final long closedTickets;
        private final long totalTickets;

        public TicketStatistics(long openTickets, long inProgressTickets, long resolvedTickets, long closedTickets, long totalTickets) {
            this.openTickets = openTickets;
            this.inProgressTickets = inProgressTickets;
            this.resolvedTickets = resolvedTickets;
            this.closedTickets = closedTickets;
            this.totalTickets = totalTickets;
        }

        // Getters
        public long getOpenTickets() { return openTickets; }
        public long getInProgressTickets() { return inProgressTickets; }
        public long getResolvedTickets() { return resolvedTickets; }
        public long getClosedTickets() { return closedTickets; }
        public long getTotalTickets() { return totalTickets; }
    }

    // Soft delete compatibility
    public void softDeleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with id: " + id));
        
        ticketRepository.deleteById(id);
    }

    public void softDeleteTicketsByUserId(Long userId) {
        List<Ticket> userTickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent();
        
        for (Ticket ticket : userTickets) {
            ticketRepository.deleteById(ticket.getId());
        }
    }
}