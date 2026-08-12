package com.magyen.platform.home.presentation.dashboard.controller;

import com.magyen.platform.home.application.dto.GetHomeDashboardQuery;
import com.magyen.platform.home.application.dto.GetHomeDashboardResult;
import com.magyen.platform.home.application.usecase.GetHomeDashboardUseCase;
import com.magyen.platform.home.presentation.dashboard.mapper.HomeDashboardPresentationMapper;
import com.magyen.platform.home.presentation.dashboard.response.HomeDashboardResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Expone el Dashboard Home como read model consolidado.
 * <p>
 * Solo lectura. No modifica datos de otros módulos.
 */
@RestController
@RequestMapping("/api/v1/home/dashboard")
public class HomeDashboardController {

    private final GetHomeDashboardUseCase getHomeDashboardUseCase;
    private final HomeDashboardPresentationMapper presentationMapper;

    public HomeDashboardController(
            GetHomeDashboardUseCase getHomeDashboardUseCase,
            HomeDashboardPresentationMapper presentationMapper
    ) {
        this.getHomeDashboardUseCase = getHomeDashboardUseCase;
        this.presentationMapper = presentationMapper;
    }

    @GetMapping
    public ResponseEntity<HomeDashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetHomeDashboardQuery query = presentationMapper.toQuery(fromDate, toDate);
        GetHomeDashboardResult result = getHomeDashboardUseCase.execute(query);
        return ResponseEntity.ok(presentationMapper.toResponse(result));
    }
}
