package com.memoria.Memoria.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for hybrid search ranking weights and relevance thresholds.
 * Bound to 'application.ai.search' in application.properties.
 */
@ConfigurationProperties(prefix = "application.ai.search")
@Getter
@Setter
public class SearchProperties {

    /** Minimum vector similarity threshold required for vector candidate matching (Default: 0.50) */
    private double minVectorSimilarity = 0.50;

    private Weights weights = new Weights();

    /**
     * Weight parameters for full-text field boosting and score blending.
     */
    @Getter
    @Setter
    public static class Weights {
        /** Weight multiplier for title match in TSVECTOR (Default: 1.0) */
        private double title = 1.0;
        
        /** Weight multiplier for AI summary match in TSVECTOR (Default: 0.8) */
        private double summary = 0.8;
        
        /** Weight multiplier for tags match in TSVECTOR (Default: 0.5) */
        private double tags = 0.5;
        
        /** Weight multiplier for content body match in TSVECTOR (Default: 0.2) */
        private double content = 0.2;
        
        /** Blend factor for text keyword score in final score (Default: 0.4) */
        private double keywordBlend = 0.4;
        
        /** Blend factor for vector similarity score in final score (Default: 0.6) */
        private double vectorBlend = 0.6;
    }
}
