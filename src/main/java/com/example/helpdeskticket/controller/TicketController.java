package com.example.helpdeskticket.controller;

import com.example.helpdeskticket.model.Ticket;
import com.example.helpdeskticket.model.TicketStatus;
import com.example.helpdeskticket.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<Page<Ticket>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Ticket> tickets = ticketService.findAll(pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> getAllTicketsList(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        List<Ticket> tickets = ticketService.findAll(sort);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.findById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        try {
            Ticket savedTicket = ticketService.createTicket(ticket);
            return ResponseEntity.ok(savedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        try {
            Ticket updatedTicket = ticketService.updateTicket(id, ticket);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Ticket>> getTicketsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketService.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<Ticket>> getAllTicketsByUser(@PathVariable Long userId) {
        // This would need a custom implementation to get user object
        // For now, using the paged version without pagination
        Page<Ticket> ticketsPage = ticketService.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        return ResponseEntity.ok(ticketsPage.getContent());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable TicketStatus status) {
        List<Ticket> tickets = ticketService.findByStatusOrderByCreatedAtAsc(status);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{status}/paged")
    public ResponseEntity<Page<Ticket>> getTicketsByStatusPaged(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketService.findByStatus(status, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/unresolved")
    public ResponseEntity<Page<Ticket>> getUnresolvedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketService.findUnresolvedTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/open")
    public ResponseEntity<List<Ticket>> getOpenTickets() {
        List<Ticket> tickets = ticketService.findOpenTickets();
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody TicketStatus status) {
        try {
            Ticket updatedTicket = ticketService.updateTicketStatus(id, status);
            return ResponseEntity.ok(updatedTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/close-resolved")
    public ResponseEntity<Void> closeResolvedTickets() {
        ticketService.closeResolvedTickets();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auto-close-old")
    public ResponseEntity<Void> autoCloseOldResolvedTickets(@RequestParam(defaultValue = "30") int daysOld) {
        ticketService.autoCloseOldResolvedTickets(daysOld);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<TicketService.TicketStatistics> getUserTicketStatistics(@PathVariable Long userId) {
        TicketService.TicketStatistics statistics = ticketService.getUserTicketStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/overall")
    public ResponseEntity<TicketService.TicketStatistics> getOverallTicketStatistics() {
        TicketService.TicketStatistics statistics = ticketService.getOverallTicketStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/average-resolution-time")
    public ResponseEntity<Double> getAverageResolutionTime() {
        double averageTime = ticketService.getAverageResolutionTime();
        return ResponseEntity.ok(averageTime);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkTicketExists(
            @RequestParam String title,
            @RequestParam Long userId) {
        // This would need user object - placeholder implementation
        return ResponseEntity.ok(false);
    }

    @GetMapping("/{ticketId}/owned-by/{userId}")
    public ResponseEntity<Boolean> isTicketOwnedByUser(
            @PathVariable Long ticketId,
            @PathVariable Long userId) {
        boolean isOwned = ticketService.isTicketOwnedByUser(ticketId, userId);
        return ResponseEntity.ok(isOwned);
    }

    @GetMapping("/user/{userId}/count-by-status")
    public ResponseEntity<Long> countTicketsByUserAndStatus(
            @PathVariable Long userId,
            @RequestParam TicketStatus status) {
        Long count = ticketService.countTicketsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(count);
    }
}