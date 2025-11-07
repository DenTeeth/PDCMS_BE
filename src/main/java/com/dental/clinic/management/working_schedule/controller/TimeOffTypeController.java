package com.dental.clinic.management.working_schedule.controller;

import com.dental.clinic.management.working_schedule.dto.response.TimeOffTypeResponse;
import com.dental.clinic.management.working_schedule.service.TimeOffTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Time-Off Type Management
 */
@RestController
@RequestMapping("/api/v1/time-off-types")
public class TimeOffTypeController {

    private static final Logger log = LoggerFactory.getLogger(TimeOffTypeController.class);

    private final TimeOffTypeService typeService;

    public TimeOffTypeController(TimeOffTypeService typeService) {
        this.typeService = typeService;
    }

    /**
     * GET /api/v1/time-off-types
     * LÃ¡ÂºÂ¥y danh sÃƒÂ¡ch tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ cÃƒÂ¡c loÃ¡ÂºÂ¡i hÃƒÂ¬nh nghÃ¡Â»â€°
     * phÃƒÂ©p Ã„â€˜ang hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng
     *
     * Authorization: YÃƒÂªu cÃ¡ÂºÂ§u Ã„â€˜ÃƒÂ£ xÃƒÂ¡c thÃ¡Â»Â±c (authenticated
     * user)
     *
     * Response:
     * - 200 OK: TrÃ¡ÂºÂ£ vÃ¡Â»Â danh sÃƒÂ¡ch cÃƒÂ¡c loÃ¡ÂºÂ¡i hÃƒÂ¬nh nghÃ¡Â»â€°
     * phÃƒÂ©p vÃ¡Â»â€ºi is_active = true
     *
     * @return List of TimeOffTypeResponse
     */
    @GetMapping
    public ResponseEntity<List<TimeOffTypeResponse>> getActiveTimeOffTypes() {
        log.info("REST request to get all active time-off types");
        List<TimeOffTypeResponse> types = typeService.getActiveTimeOffTypes();
        return ResponseEntity.ok(types);
    }
}
