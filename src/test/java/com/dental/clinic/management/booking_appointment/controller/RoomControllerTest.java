package com.dental.clinic.management.booking_appointment.controller;

import com.dental.clinic.management.booking_appointment.dto.request.CreateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.request.UpdateRoomRequest;
import com.dental.clinic.management.booking_appointment.dto.response.RoomResponse;
import com.dental.clinic.management.booking_appointment.service.RoomService;
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
 * Unit tests for RoomController
 * Testing REST API endpoints for Room Management
 */
@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
class RoomControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RoomService roomService;

        private RoomResponse roomResponse1;
        private RoomResponse roomResponse2;
        private CreateRoomRequest createRequest;
        private UpdateRoomRequest updateRequest;

        @BeforeEach
        void setUp() {
                // Sample RoomResponse objects
                LocalDateTime now = LocalDateTime.now();
                roomResponse1 = new RoomResponse("ROOM001", "P-01", "Phòng thường 1", "STANDARD", true, now, now);

                roomResponse2 = new RoomResponse("ROOM002", "P-02", "Phòng Implant", "IMPLANT", true, now, now);

                // Sample CreateRoomRequest
                createRequest = new CreateRoomRequest();
                createRequest.setRoomCode("P-03");
                createRequest.setRoomName("Phòng VIP");
                createRequest.setRoomType("VIP");

                // Sample UpdateRoomRequest
                updateRequest = new UpdateRoomRequest();
                updateRequest.setRoomName("Phòng thường 1 - Đã sửa");
                updateRequest.setRoomType("STANDARD");
                updateRequest.setIsActive(true);
        }

        @Test
        @DisplayName("GET /api/v1/rooms - Get all rooms with pagination")
        @WithMockUser(authorities = { "VIEW_ROOM" })
        void getAllRooms_ShouldReturnPagedRooms() throws Exception {
                // Given
                Page<RoomResponse> page = new PageImpl<>(Arrays.asList(roomResponse1, roomResponse2));
                when(roomService.getAllRooms(anyInt(), anyInt(), anyString(), anyString(),
                                isNull(), isNull(), isNull())).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/v1/rooms")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sortBy", "roomId")
                                .param("sortDirection", "ASC")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.content[0].roomCode", is("P-01")))
                                .andExpect(jsonPath("$.content[0].roomName", is("Phòng thường 1")))
                                .andExpect(jsonPath("$.content[1].roomCode", is("P-02")));

                verify(roomService, times(1)).getAllRooms(0, 10, "roomId", "ASC", null, null, null);
        }

        @Test
        @DisplayName("GET /api/v1/rooms - Get rooms with filters")
        @WithMockUser(authorities = { "VIEW_ROOM" })
        void getAllRooms_WithFilters_ShouldReturnFilteredRooms() throws Exception {
                // Given
                Page<RoomResponse> page = new PageImpl<>(List.of(roomResponse1));
                when(roomService.getAllRooms(anyInt(), anyInt(), anyString(), anyString(),
                                eq(true), eq("STANDARD"), eq("phòng"))).thenReturn(page);

                // When & Then
                mockMvc.perform(get("/api/v1/rooms")
                                .param("page", "0")
                                .param("size", "10")
                                .param("isActive", "true")
                                .param("roomType", "STANDARD")
                                .param("keyword", "phòng")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].roomType", is("STANDARD")));

                verify(roomService, times(1)).getAllRooms(0, 10, "roomId", "ASC", true, "STANDARD", "phòng");
        }

        @Test
        @DisplayName("GET /api/v1/rooms/active - Get all active rooms")
        @WithMockUser(authorities = { "VIEW_ROOM" })
        void getAllActiveRooms_ShouldReturnActiveRoomsList() throws Exception {
                // Given
                List<RoomResponse> activeRooms = Arrays.asList(roomResponse1, roomResponse2);
                when(roomService.getAllActiveRooms()).thenReturn(activeRooms);

                // When & Then
                mockMvc.perform(get("/api/v1/rooms/active")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[0].isActive", is(true)))
                                .andExpect(jsonPath("$[1].isActive", is(true)));

                verify(roomService, times(1)).getAllActiveRooms();
        }

        @Test
        @DisplayName("GET /api/v1/rooms/{roomId} - Get room by ID")
        @WithMockUser(authorities = { "VIEW_ROOM" })
        void getRoomById_ShouldReturnRoom() throws Exception {
                // Given
                when(roomService.getRoomById("ROOM001")).thenReturn(roomResponse1);

                // When & Then
                mockMvc.perform(get("/api/v1/rooms/{roomId}", "ROOM001")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.roomId", is("ROOM001")))
                                .andExpect(jsonPath("$.roomCode", is("P-01")))
                                .andExpect(jsonPath("$.roomName", is("Phòng thường 1")));

                verify(roomService, times(1)).getRoomById("ROOM001");
        }

        @Test
        @DisplayName("POST /api/v1/rooms - Create new room")
        @WithMockUser(authorities = { "CREATE_ROOM" })
        void createRoom_ShouldReturnCreatedRoom() throws Exception {
                // Given
                LocalDateTime now = LocalDateTime.now();
                RoomResponse createdRoom = new RoomResponse("ROOM003", "P-03", "Phòng VIP", "VIP", true, now, now);

                when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(createdRoom);

                // When & Then
                mockMvc.perform(post("/api/v1/rooms")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.roomId", is("ROOM003")))
                                .andExpect(jsonPath("$.roomCode", is("P-03")))
                                .andExpect(jsonPath("$.roomName", is("Phòng VIP")))
                                .andExpect(jsonPath("$.roomType", is("VIP")));

                verify(roomService, times(1)).createRoom(any(CreateRoomRequest.class));
        }

        @Test
        @DisplayName("POST /api/v1/rooms - Create room with invalid data should fail")
        @WithMockUser(authorities = { "CREATE_ROOM" })
        void createRoom_WithInvalidData_ShouldReturnBadRequest() throws Exception {
                // Given - empty roomCode
                CreateRoomRequest invalidRequest = new CreateRoomRequest();
                invalidRequest.setRoomCode("");
                invalidRequest.setRoomName("Test");
                invalidRequest.setRoomType("STANDARD");

                // When & Then
                mockMvc.perform(post("/api/v1/rooms")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andDo(print())
                                .andExpect(status().isBadRequest());

                verify(roomService, never()).createRoom(any(CreateRoomRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/rooms/{roomId} - Update room")
        @WithMockUser(authorities = { "UPDATE_ROOM" })
        void updateRoom_ShouldReturnUpdatedRoom() throws Exception {
                // Given
                LocalDateTime now = LocalDateTime.now();
                RoomResponse updatedRoom = new RoomResponse("ROOM001", "P-01", "Phòng thường 1 - Đã sửa", "STANDARD",
                                true, now, now);

                when(roomService.updateRoom(eq("ROOM001"), any(UpdateRoomRequest.class))).thenReturn(updatedRoom);

                // When & Then
                mockMvc.perform(put("/api/v1/rooms/{roomId}", "ROOM001")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.roomId", is("ROOM001")))
                                .andExpect(jsonPath("$.roomName", is("Phòng thường 1 - Đã sửa")));

                verify(roomService, times(1)).updateRoom(eq("ROOM001"), any(UpdateRoomRequest.class));
        }

        @Test
        @DisplayName("DELETE /api/v1/rooms/{roomId} - Soft delete room")
        @WithMockUser(authorities = { "DELETE_ROOM" })
        void deleteRoom_ShouldReturnNoContent() throws Exception {
                // Given
                doNothing().when(roomService).deleteRoom("ROOM001");

                // When & Then
                mockMvc.perform(delete("/api/v1/rooms/{roomId}", "ROOM001")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                verify(roomService, times(1)).deleteRoom("ROOM001");
        }

        @Test
        @DisplayName("DELETE /api/v1/rooms/{roomId}/permanent - Permanently delete room")
        @WithMockUser(roles = { "ADMIN" })
        void permanentlyDeleteRoom_ShouldReturnNoContent() throws Exception {
                // Given
                doNothing().when(roomService).permanentlyDeleteRoom("ROOM001");

                // When & Then
                mockMvc.perform(delete("/api/v1/rooms/{roomId}/permanent", "ROOM001")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                verify(roomService, times(1)).permanentlyDeleteRoom("ROOM001");
        }

        @Test
        @DisplayName("DELETE /api/v1/rooms/{roomId}/permanent - Order matters for path matching")
        @WithMockUser(roles = { "ADMIN" })
        void permanentDelete_ShouldMatchCorrectEndpoint() throws Exception {
                // Given
                doNothing().when(roomService).permanentlyDeleteRoom("ROOM001");

                // When & Then - This should match /permanent endpoint, NOT /{roomId}
                mockMvc.perform(delete("/api/v1/rooms/{roomId}/permanent", "ROOM001")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                                .andDo(print())
                                .andExpect(status().isNoContent());

                // Verify that permanentlyDeleteRoom was called, NOT deleteRoom
                verify(roomService, times(1)).permanentlyDeleteRoom("ROOM001");
                verify(roomService, never()).deleteRoom(anyString());
        }
}
