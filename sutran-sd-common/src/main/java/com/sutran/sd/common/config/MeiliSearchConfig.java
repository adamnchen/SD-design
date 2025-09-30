package com.sutran.sd.common.config;

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeiliSearchConfig {


    @Value("${meilisearch.host}")
    private String meiliSearchHost;

    @Value("${meilisearch.api-key}")
    private String meiliSearchApiKey;


    @Bean
    public Client meiliSearchClient() {

        Config config = new Config(meiliSearchHost, meiliSearchApiKey);


        return new Client(config);
    }
}
