package com.example.aws_cost_analyser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

@Configuration
public class AwsConfig {

    @Bean
    public CostExplorerClient costExplorerClient() {
        return CostExplorerClient.builder()
                .region(Region.US_EAST_1) // Cost Explorer is global
                .build(); // Uses default credential chain
    }
}
