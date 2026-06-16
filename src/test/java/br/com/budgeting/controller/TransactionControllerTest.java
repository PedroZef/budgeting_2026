package br.com.budgeting.controller;

import br.com.budgeting.model.Transaction;
import br.com.budgeting.service.TransactionService;
import br.com.budgeting.security.JwtAuthenticationFilter;
import br.com.budgeting.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService service;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @WithMockUser(username = "pedro")
    void deveListarTransacoesDoUsuarioComSucesso() throws Exception {
        // Given
        Transaction t = new Transaction();
        t.setId(1L);
        t.setUsuario("pedro");
        t.setValor(new BigDecimal("150.00"));
        t.setCategoria("Lazer");
        t.setTipo("DESPESA");

        when(service.listarPorUsuario("pedro")).thenReturn(Collections.singletonList(t));

        // When & Then
        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Lazer"))
                .andExpect(jsonPath("$[0].valor").value(150.00))
                .andExpect(jsonPath("$[0].tipo").value("DESPESA"));
    }

    @Test
    @WithMockUser(username = "pedro")
    void deveCriarTransacaoComSucesso() throws Exception {
        // Given
        Transaction t = new Transaction();
        t.setValor(new BigDecimal("200.00"));
        t.setCategoria("Alimentação");
        t.setTipo("DESPESA");

        Transaction salva = new Transaction();
        salva.setId(2L);
        salva.setUsuario("pedro");
        salva.setValor(new BigDecimal("200.00"));
        salva.setCategoria("Alimentação");
        salva.setTipo("DESPESA");

        when(service.salvar(any(Transaction.class))).thenReturn(salva);

        // When & Then
        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.categoria").value("Alimentação"));
    }

    @Test
    @WithMockUser(username = "pedro")
    void deveAtualizarTransacaoComSucesso() throws Exception {
        // Given
        Transaction t = new Transaction();
        t.setValor(new BigDecimal("100.00"));
        t.setCategoria("Alimentação");
        t.setTipo("DESPESA");

        Transaction atualizada = new Transaction();
        atualizada.setId(1L);
        atualizada.setUsuario("pedro");
        atualizada.setValor(new BigDecimal("100.00"));
        atualizada.setCategoria("Alimentação");
        atualizada.setTipo("DESPESA");

        when(service.atualizar(eq(1L), any(Transaction.class))).thenReturn(atualizada);

        // When & Then
        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(t)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.categoria").value("Alimentação"));
    }

    @Test
    @WithMockUser(username = "pedro")
    void deveDeletarTransacaoComSucesso() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/transactions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
