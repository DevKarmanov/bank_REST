package com.example.bankcards.controller;

import com.example.bankcards.controller.card.CardControllerImpl;
import com.example.bankcards.dto.response.card.CardDtoResponse;
import com.example.bankcards.entity.card.State;
import com.example.bankcards.security.service.MyUserDetailsService;
import com.example.bankcards.security.service.jwt.JwtService;
import com.example.bankcards.service.card.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = CardControllerImpl.class)
class CardControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MyUserDetailsService myUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ShouldReturnCreatedCard() throws Exception {
        CardDtoResponse response = new CardDtoResponse(1L, "1234...", "**** **** **** 1234",
                LocalDate.now().plusYears(3), State.ACTIVE, BigDecimal.ZERO);

        when(cardService.createCard("admin")).thenReturn(response);

        mockMvc.perform(post("/api/v1/card/create").with(csrf())
                        .param("ownerName", "admin"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleCardState_ShouldReturnSuccessMessage() throws Exception {
        when(cardService.toggleCardState(1L)).thenReturn("You have successfully blocked the card");

        mockMvc.perform(patch("/api/v1/card/toggle/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("blocked")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/card/1").with(csrf()))
                .andExpect(status().isOk());

        verify(cardService).delCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/card/all")
                        .param("limit", "10")
                        .param("pageNumber", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user",roles = "USER")
    void createCard_ShouldReturnForbidden_WhenUserRole() throws Exception {
        mockMvc.perform(post("/api/v1/card/create")
                        .param("ownerName", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ShouldReturnServerError_WhenExceptionThrown() throws Exception {
        when(cardService.createCard(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/v1/card/create").with(csrf())
                        .param("ownerName", "admin"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateCard_ForbiddenIfNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/card/create")
                        .with(csrf())
                        .param("ownerName", "admin"))
                .andExpect(status().isCreated());
    }

}
