package com.dental.clinic.management.working_schedule.service;

import com.dental.clinic.management.working_schedule.dto.response.TimeOffTypeResponse;
import com.dental.clinic.management.working_schedule.mapper.TimeOffTypeMapper;
import com.dental.clinic.management.working_schedule.repository.TimeOffTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing time-off types
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimeOffTypeService {

    private final TimeOffTypeRepository typeRepository;
    private final TimeOffTypeMapper typeMapper;

    /**
     * GET /api/v1/time-off-types
     * Lấy danh sách tất cả các loại hình nghỉ phép đang hoạt động
     */
    public List<TimeOffTypeResponse> getActiveTimeOffTypes() {
        log.debug("Request to get all active time-off types");

        return typeRepository.findByIsActiveTrue()
                .stream()
                .map(typeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
