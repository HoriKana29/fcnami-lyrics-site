package com.fcnami.backend.Api;

import java.util.List;

/**
 * RulesDtos contains the data transfer objects for the request rules and FAQ system.
 */
public class RulesDtos {

    /**
     * RulesResponse represents the complete set of request rules and FAQs.
     */
    public record RulesResponse(
            String heroTitle,
            String heroSubtitle,
            List<RuleStep> steps,
            List<FAQItem> faqs
    ) {}

    /**
     * RuleStep represents a single step in the request process.
     */
    public record RuleStep(
            String icon,
            String title,
            String description
    ) {}

    /**
     * FAQItem represents a frequently asked question and its answer.
     */
    public record FAQItem(
            String question,
            String answer
    ) {}
}
