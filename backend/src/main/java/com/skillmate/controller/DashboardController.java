package com.skillmate.controller;

import com.skillmate.dto.DashboardDtos.*;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping("/me")
    public DashboardSummary me(@AuthenticationPrincipal UserPrincipal me) { return dashboard.me(me.getId()); }

    @GetMapping("/me/progress")
    public ProgressOverview progress(@AuthenticationPrincipal UserPrincipal me) { return dashboard.progress(me.getId()); }

    @GetMapping("/me/readiness")
    public ReadinessDto readiness(@AuthenticationPrincipal UserPrincipal me) { return dashboard.readiness(me.getId()); }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStats admin() { return dashboard.admin(); }
}
