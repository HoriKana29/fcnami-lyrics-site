package com.fcnami.backend.Api;

import com.fcnami.backend.Api.RulesDtos.RulesResponse;
import com.fcnami.backend.Service.RulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RulesController provides an endpoint to retrieve the song request rules and FAQs.
 */
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RulesController {
    private final RulesService rulesService;

    /**
     * Retrieves the request rules and FAQs.
     * @return RulesResponse containing the structured rules content.
     */
    @GetMapping
    public RulesResponse getRules() {
        return rulesService.getRules();
    }
}
