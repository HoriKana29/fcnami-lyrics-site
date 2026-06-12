package com.fcnami.backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcnami.backend.Api.RulesDtos.RulesResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * RulesService handles the loading and serving of request rules and FAQs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {
    private final ObjectMapper objectMapper;
    private RulesResponse cachedRules;

    /**
     * Initializes the service by loading rules from the JSON configuration file.
     * Postcondition: cachedRules is populated with data from rules.json.
     */
    @PostConstruct
    public void init() {
        loadRules();
    }

    /**
     * Loads the rules from the classpath resource.
     * Side-effect: Updates the cachedRules variable.
     */
    public void loadRules() {
        try {
            ClassPathResource resource = new ClassPathResource("rules.json");
            cachedRules = objectMapper.readValue(resource.getInputStream(), RulesResponse.class);
            log.info("Successfully loaded rules from rules.json");
        } catch (IOException e) {
            log.error("Failed to load rules from rules.json", e);
            // Fallback or rethrow depending on criticality
            throw new RuntimeException("Could not initialize request rules", e);
        }
    }

    /**
     * Returns the currently loaded request rules.
     * @return RulesResponse containing the rules and FAQs.
     */
    public RulesResponse getRules() {
        return cachedRules;
    }
}
