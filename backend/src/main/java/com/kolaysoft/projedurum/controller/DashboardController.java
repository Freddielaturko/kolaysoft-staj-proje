package com.kolaysoft.projedurum.controller;

import com.kolaysoft.projedurum.dto.AlertResponse;
import com.kolaysoft.projedurum.dto.DashboardSummaryResponse;
import com.kolaysoft.projedurum.security.UserPrincipal;
import com.kolaysoft.projedurum.service.ProjectInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Her rol yalnizca erisebildigi projeler uzerinden ozet ve uyari gorur
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectInsightService insightService;

    @GetMapping("/api/dashboard/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal UserPrincipal principal) {
        return insightService.summary(insightService.accessibleProjects(principal));
    }

    @GetMapping("/api/alerts")
    public List<AlertResponse> alerts(@AuthenticationPrincipal UserPrincipal principal) {
        return insightService.alerts(insightService.accessibleProjects(principal));
    }
}
