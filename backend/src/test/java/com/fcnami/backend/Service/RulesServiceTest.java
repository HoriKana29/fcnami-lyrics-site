package com.fcnami.backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcnami.backend.Api.RulesDtos.RulesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RulesServiceTest {

    private RulesService rulesService;

    @BeforeEach
    void setUp() {
        rulesService = new RulesService(new ObjectMapper());
        rulesService.init();
    }

    @Test
    void shouldLoadRulesFromJson() {
        RulesResponse rules = rulesService.getRules();
        assertThat(rules).isNotNull();
        assertThat(rules.heroTitle()).isEqualTo("Request a Song");
        assertThat(rules.steps()).isNotEmpty();
        assertThat(rules.faqs()).isNotEmpty();
    }
}
