package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.request.CreateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateServiceRequest;
import com.dental.clinic.management.booking_appointment.dto.response.ServiceResponse;
import com.dental.clinic.management.booking_appointment.service.DentalServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ServiceController
 * Testing REST API endpoints for Service Management
 */
@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class ServiceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private DentalServiceService serviceService;

        private ServiceResponse serviceResponse1;
        private ServiceResponse serviceResponse2;
        private CreateServiceRequest createRequest;
        private UpdateServiceRequest updateRequest;

        @BeforeEach
        void setUp() {
                // Sample ServiceResponse objects
                serviceResponse1 = ServiceResponse.builder()
                                .serviceId(1)
                                .serviceCode("SCALING_L1")
                                .serviceName("Cạo vôi răng & Đánh bóng - Mức 1")
                                .description("Làm sạch vôi răng và mảng bám mức độ ít/trung bình.")
                                .defaultDurationMinutes(45)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("300000"))
                                .specializationId(null)
                                .specializationName(null)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                serviceResponse2 = ServiceResponse.builder()
                                .serviceId(2)
                                .serviceCode("CROWN_TITAN")
                                .serviceName("Mão răng sứ Titan")
                                .description("Mão sứ sườn hợp kim Titan.")
                                .defaultDurationMinutes(60)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("2500000"))
                                .specializationId(3)
                                .specializationName("Răng thẩm mỹ")
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                // Sample CreateServiceRequest
                createRequest = CreateServiceRequest.builder()
                                .serviceCode("FILLING_COMP")
                                .serviceName("Trám răng Composite")
                                .description("Trám răng sâu, mẻ bằng vật liệu composite thẩm mỹ.")
                                .defaultDurationMinutes(45)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("400000"))
                                .specializationId(null)
                                .isActive(true)
                                .build();

                // Sample UpdateServiceRequest
                updateRequest = UpdateServiceRequest.builder()
                                .serviceName("Cạo vôi răng & Đánh bóng - Mức 1 (Đã cập nhật)")
                                .price(new BigDecimal("350000"))
                                .isActive(true)
                                .build();
        }

        @Test
        @DisplayName("GET /api/v1/services - Get all services with pagination")
        @WithMockUser(authorities = { "VIEW_SERVICE" })
        void getAllServices_ShouldReturnPagedServices() throws Exception {
                // Given
                Page<ServiceResponse> page = new PageImpl<>(Arrays.asList(serviceResponse1, serviceResponse2));
                when(serviceService.getAllServices(anyInt(), anyInt(), anyString(), anyString(),
                                isNull(), isNull(), isNull())).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/v1/services")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "serviceName")
                                .param("sortDirection", "ASC")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[0].serviceCode", is("SCALING_L1")))
                                .andExpect(jsonPath("$.content[0].serviceName", containsString("Cạo vôi")))
                                .andExpect(jsonPath("$.content[1].serviceCode", is("CROWN_TITAN")));

                verify(serviceService, times(1)).getAllServices(0, 10, "serviceName", "ASC", null, null, null);
        }

        @Test
        @DisplayName("GET /api/v1/services - Get services with filters")
        @WithMockUser(authorities = { "VIEW_SERVICE" })
        void getAllServices_WithFilters_ShouldReturnFilteredServices() throws Exception {
                // Given
                Page<ServiceResponse> page = new PageImpl<>(List.of(serviceResponse2));
                when(serviceService.getAllServices(anyInt(), anyInt(), anyString(), anyString(),
                                eq(true), eq(3), eq("titan"))).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/v1/services")
                                .param("page", "0")
                                .param("size", "10")
                                .param("isActive", "true")
                                .param("specializationId", "3")
                                .param("keyword", "titan")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].specializationId", is(3)))
                                .andExpect(jsonPath("$.content[0].serviceName", containsString("Titan")));

                verify(serviceService, times(1)).getAllServices(0, 10, "serviceName", "ASC", true, 3, "titan");
        }

        @Test
        @DisplayName("GET /api/v1/services/{serviceId} - Get service by ID")
        @WithMockUser(authorities = { "VIEW_SERVICE" })
        void getServiceById_ShouldReturnService() throws Exception {
                // Given
                when(serviceService.getServiceById(1)).thenReturn(serviceResponse1);

                // When & Then
                mockMvc.perform(get("/api/v1/services/{serviceId}", 1)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceId", is(1)))
                                .andExpect(jsonPath("$.serviceCode", is("SCALING_L1")))
                                .andExpect(jsonPath("$.serviceName", containsString("Cạo vôi")))
                                .andExpect(jsonPath("$.price", is(300000)));

                verify(serviceService, times(1)).getServiceById(1);
        }

        @Test
        @DisplayName("GET /api/v1/services/code/{serviceCode} - Get service by code")
        @WithMockUser(authorities = { "VIEW_SERVICE" })
        void getServiceByCode_ShouldReturnService() throws Exception {
                // Given
                when(serviceService.getServiceByCode("CROWN_TITAN")).thenReturn(serviceResponse2);

                // When & Then
                mockMvc.perform(get("/api/v1/services/code/{serviceCode}", "CROWN_TITAN")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceId", is(2)))
                                .andExpect(jsonPath("$.serviceCode", is("CROWN_TITAN")))
                                .andExpect(jsonPath("$.serviceName", containsString("Titan")))
                                .andExpect(jsonPath("$.specializationId", is(3)));

                verify(serviceService, times(1)).getServiceByCode("CROWN_TITAN");
        }

        @Test
        @DisplayName("GET /api/v1/services/code/{serviceCode} - Should not conflict with /{serviceId}")
        @WithMockUser(authorities = { "VIEW_SERVICE" })
        void getServiceByCode_ShouldNotConflictWithGetById() throws Exception {
                // Given - Testing that /code/{serviceCode} path works for String codes
                when(serviceService.getServiceByCode("SCALING_L1")).thenReturn(serviceResponse1);

                // When & Then - /code/SCALING_L1 should match getServiceByCode, NOT
                // getServiceById
                mockMvc.perform(get("/api/v1/services/code/{serviceCode}", "SCALING_L1")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceCode", is("SCALING_L1")));

                // Verify getServiceByCode was called, NOT getServiceById
                verify(serviceService, times(1)).getServiceByCode("SCALING_L1");
                verify(serviceService, never()).getServiceById(anyInt());
        }

        @Test
        @DisplayName("POST /api/v1/services - Create new service")
        @WithMockUser(authorities = { "CREATE_SERVICE" })
        void createService_ShouldReturnCreatedService() throws Exception {
                // Given
                ServiceResponse createdService = ServiceResponse.builder()
                                .serviceId(3)
                                .serviceCode("FILLING_COMP")
                                .serviceName("Trám răng Composite")
                                .description("Trám răng sâu, mẻ bằng vật liệu composite thẩm mỹ.")
                                .defaultDurationMinutes(45)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("400000"))
                                .specializationId(null)
                                .specializationName(null)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                when(serviceService.createService(any(CreateServiceRequest.class))).thenReturn(createdService);

                // When & Then
                mockMvc.perform(post("/api/v1/services")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.serviceId", is(3)))
                                .andExpect(jsonPath("$.serviceCode", is("FILLING_COMP")))
                                .andExpect(jsonPath("$.serviceName", containsString("Composite")))
                                .andExpect(jsonPath("$.price", is(400000)));

                verify(serviceService, times(1)).createService(any(CreateServiceRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/services - Create service with invalid data should fail")
        @WithMockUser(authorities = { "CREATE_SERVICE" })
        void createService_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // Given - negative price
                CreateServiceRequest invalidRequest = CreateServiceRequest.builder()
                                .serviceCode("TEST")
                                .serviceName("Test Service")
                                .defaultDurationMinutes(30)
                                .defaultBufferMinutes(10)
                                .price(new BigDecimal("-100")) // Invalid: negative price
                                .isActive(true)
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/services")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(serviceService, never()).createService(any(CreateServiceRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/services/{serviceCode} - Update service")
        @WithMockUser(authorities = { "UPDATE_SERVICE" })
        void updateService_ShouldReturnUpdatedService() throws Exception {
                // Given
                ServiceResponse updatedService = ServiceResponse.builder()
                                .serviceId(1)
                                .serviceCode("SCALING_L1")
                                .serviceName("Cạo vôi răng & Đánh bóng - Mức 1 (Đã cập nhật)")
                                .description("Làm sạch vôi răng và mảng bám mức độ ít/trung bình.")
                                .defaultDurationMinutes(45)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("350000"))
                                .specializationId(null)
                                .specializationName(null)
                                .isActive(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                when(serviceService.updateService(eq("SCALING_L1"), any(UpdateServiceRequest.class)))
                                .thenReturn(updatedService);

                // When & Then
                mockMvc.perform(put("/api/v1/services/{serviceCode}", "SCALING_L1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceId", is(1)))
                                .andExpect(jsonPath("$.serviceName", containsString("Đã cập nhật")))
                                .andExpect(jsonPath("$.price", is(350000)));

                verify(serviceService, times(1)).updateService(eq("SCALING_L1"), any(UpdateServiceRequest.class));
        }

        @Test
        @DisplayName("DELETE /api/v1/services/{serviceCode} - Soft delete service")
        @WithMockUser(authorities = { "DELETE_SERVICE" })
        void deleteService_ShouldReturnNoContent() throws Exception {
                // Given
                doNothing().when(serviceService).deleteService("SCALING_L1");

                // When & Then
                mockMvc.perform(delete("/api/v1/services/{serviceCode}", "SCALING_L1")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                verify(serviceService, times(1)).deleteService("SCALING_L1");
        }

        @Test
        @DisplayName("PATCH /api/v1/services/{serviceId}/activate - Activate service")
        @WithMockUser(authorities = { "UPDATE_SERVICE" })
        void activateService_ShouldReturnActivatedService() throws Exception {
                // Given
                ServiceResponse activatedService = ServiceResponse.builder()
                                .serviceId(1)
                                .serviceCode("SCALING_L1")
                                .serviceName("Cạo vôi răng & Đánh bóng - Mức 1")
                                .description("Làm sạch vôi răng và mảng bám mức độ ít/trung bình.")
                                .defaultDurationMinutes(45)
                                .defaultBufferMinutes(15)
                                .price(new BigDecimal("300000"))
                                .specializationId(null)
                                .specializationName(null)
                                .isActive(true) // Now active
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                doNothing().when(serviceService).activateService(1);
                when(serviceService.getServiceById(1)).thenReturn(activatedService);

                // When & Then
                mockMvc.perform(patch("/api/v1/services/{serviceId}/activate", 1)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceId", is(1)))
                                .andExpect(jsonPath("$.isActive", is(true)));

                verify(serviceService, times(1)).activateService(1);
                verify(serviceService, times(1)).getServiceById(1);
        }

        @Test
        @DisplayName("PATCH /api/v1/services/{serviceId}/activate - Endpoint path should be correct")
        @WithMockUser(authorities = { "UPDATE_SERVICE" })
        void activateService_ShouldMatchCorrectEndpoint() throws Exception {
                // Given
                ServiceResponse activatedService = ServiceResponse.builder()
                                .serviceId(109)
                                .serviceCode("TEST_SERVICE")
                                .serviceName("Test Service")
                                .isActive(true)
                                .price(new BigDecimal("100000"))
                                .defaultDurationMinutes(30)
                                .defaultBufferMinutes(10)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                doNothing().when(serviceService).activateService(109);
                when(serviceService.getServiceById(109)).thenReturn(activatedService);

                // When & Then - Testing /109/activate (NOT /109/active which was the error)
                mockMvc.perform(patch("/api/v1/services/{serviceId}/activate", 109)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.serviceId", is(109)))
                                .andExpect(jsonPath("$.isActive", is(true)));

                verify(serviceService, times(1)).activateService(109);
        }
}
