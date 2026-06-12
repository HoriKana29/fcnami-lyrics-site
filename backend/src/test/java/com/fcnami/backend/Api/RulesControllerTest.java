package com.fcnami.backend.Api;

import com.fcnami.backend.Api.RulesDtos.RulesResponse;
import com.fcnami.backend.Service.RulesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RulesControllerTest {

    @Mock
    private RulesService rulesService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RulesController(rulesService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnRules() throws Exception {
        RulesResponse mockResponse = new RulesResponse(
                "Hero Title",
                "Hero Subtitle",
                List.of(new RulesDtos.RuleStep("Icon", "Step 1", "Desc 1")),
                List.of(new RulesDtos.FAQItem("Q1", "A1"))
        );

        when(rulesService.getRules()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroTitle").value("Hero Title"))
                .andExpect(jsonPath("$.steps[0].title").value("Step 1"))
                .andExpect(jsonPath("$.faqs[0].question").value("Q1"));
    }
}
