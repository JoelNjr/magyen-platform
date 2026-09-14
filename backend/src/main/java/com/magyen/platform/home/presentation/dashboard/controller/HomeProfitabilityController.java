package com.magyen.platform.home.presentation.dashboard.controller;

import com.magyen.platform.home.application.dto.GetHomeProfitabilityQuery;
import com.magyen.platform.home.application.dto.GetHomeProfitabilityResult;
import com.magyen.platform.home.application.usecase.GetHomeProfitabilityUseCase;
import com.magyen.platform.home.presentation.dashboard.mapper.HomeDashboardPresentationMapper;
import com.magyen.platform.home.presentation.dashboard.response.HomeProfitabilityResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Expone la rentabilidad Home de forma independiente del resto del dashboard.
 */
@RestController
@RequestMapping("/api/v1/home/profitability")
public class HomeProfitabilityController {

    private final GetHomeProfitabilityUseCase getHomeProfitabilityUseCase;
    private final HomeDashboardPresentationMapper presentationMapper;

    public HomeProfitabilityController(
            GetHomeProfitabilityUseCase getHomeProfitabilityUseCase,
            HomeDashboardPresentationMapper presentationMapper
    ) {
        this.getHomeProfitabilityUseCase = getHomeProfitabilityUseCase;
        this.presentationMapper = presentationMapper;
    }

    @GetMapping
    public ResponseEntity<HomeProfitabilityResponse> getProfitability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        GetHomeProfitabilityResult result = getHomeProfitabilityUseCase.execute(
                new GetHomeProfitabilityQuery(fromDate, toDate)
        );
        return ResponseEntity.ok(presentationMapper.toProfitabilityResponse(result));
    }
}
