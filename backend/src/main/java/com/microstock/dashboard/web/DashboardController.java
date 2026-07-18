package com.microstock.dashboard.web;

import com.microstock.dashboard.service.DashboardService;
import com.microstock.dashboard.web.dto.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** Owner-scoped for users, system-wide for admins. */
    @GetMapping
    public DashboardResponse dashboard() {
        return service.compute();
    }
}
