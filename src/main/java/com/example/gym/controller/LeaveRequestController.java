package com.example.gym.controller;

import com.example.gym.dto.LeaveRequestDTO;
import com.example.gym.entity.LeaveRequest;
import com.example.gym.entity.Staff;
import com.example.gym.entity.User;
import com.example.gym.repository.LeaveRequestRepository;
import com.example.gym.repository.StaffRepository;
import com.example.gym.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final com.example.gym.service.NotificationService notificationService;

    public LeaveRequestController(LeaveRequestRepository leaveRequestRepository, StaffRepository staffRepository, UserRepository userRepository, com.example.gym.service.NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /** POST /api/leaves/apply/{userId} — Employee applies for leave */
    @PostMapping("/apply/{userId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> applyLeave(@PathVariable("userId") Long userId, @RequestBody LeaveRequestDTO dto) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found with id: " + userId));
            }
            if (user.getStaffDetails() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User " + userId + " has no staff record"));
            }

            // Re-fetch staff directly to avoid any lazy-load or detached entity issues
            Staff staff = staffRepository.findById(user.getStaffDetails().getId())
                    .orElseThrow(() -> new RuntimeException("Staff record missing for user " + userId));

            LeaveRequest req = new LeaveRequest();
            req.setStaff(staff);
            req.setStartDate(dto.getStartDate());
            req.setEndDate(dto.getEndDate());
            req.setLeaveType(dto.getLeaveType());
            req.setReason(dto.getReason());
            req.setStatus("PENDING");
            req.setAppliedAt(java.time.LocalDateTime.now());

            LeaveRequest saved = leaveRequestRepository.save(req);
            leaveRequestRepository.flush();

            notificationService.broadcast("LEAVE_REQUEST", Map.of(
                    "userId", userId,
                    "name", user.getFullName() != null ? user.getFullName() : "Employee",
                    "leaveType", dto.getLeaveType(),
                    "reason", dto.getReason() != null ? dto.getReason() : "No reason provided"
            ));

            // Build DTO manually — avoids any Jackson serialization issues with JPA proxies
            LeaveRequestDTO result = new LeaveRequestDTO();
            result.setId(saved.getId());
            result.setStaffId(staff.getId());
            result.setStaffName(user.getFullName());
            result.setStartDate(saved.getStartDate());
            result.setEndDate(saved.getEndDate());
            result.setLeaveType(saved.getLeaveType());
            result.setReason(saved.getReason());
            result.setStatus(saved.getStatus());
            result.setAppliedAt(saved.getAppliedAt());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage() != null ? e.getMessage() : "Unknown error",
                "cause", e.getCause() != null ? e.getCause().getMessage() : "none",
                "type", e.getClass().getName()
            ));
        }
    }

    /** GET /api/leaves/staff/{userId} — Get leaves for a specific employee */
    @GetMapping("/staff/{userId}")
    public ResponseEntity<?> getLeavesByStaff(@PathVariable("userId") Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getStaffDetails() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Staff not found"));
        }

        List<LeaveRequestDTO> list = leaveRequestRepository.findByStaffIdOrderByAppliedAtDesc(user.getStaffDetails().getId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** GET /api/leaves — Get all leaves (Admin view) */
    @GetMapping
    public List<LeaveRequestDTO> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByAppliedAtDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /** PUT /api/leaves/{id}/status — Admin approve/reject, or Employee cancel */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateLeaveStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        return leaveRequestRepository.findById(id).map(req -> {
            String newStatus = payload.get("status");
            if (newStatus != null) {
                if ("CANCELLED".equals(newStatus) && !"PENDING".equals(req.getStatus())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Only PENDING leaves can be cancelled"));
                }
                req.setStatus(newStatus);
            }
            LeaveRequest saved = leaveRequestRepository.save(req);
            return ResponseEntity.ok(mapToDTO(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    private LeaveRequestDTO mapToDTO(LeaveRequest req) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(req.getId());
        if (req.getStaff() != null) {
            dto.setStaffId(req.getStaff().getId());
            if (req.getStaff().getUser() != null) {
                dto.setStaffName(req.getStaff().getUser().getFullName());
            }
        }
        dto.setStartDate(req.getStartDate());
        dto.setEndDate(req.getEndDate());
        dto.setLeaveType(req.getLeaveType());
        dto.setReason(req.getReason());
        dto.setStatus(req.getStatus());
        dto.setAppliedAt(req.getAppliedAt());
        return dto;
    }
}
