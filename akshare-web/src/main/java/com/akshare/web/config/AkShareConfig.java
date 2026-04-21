package com.akshare.web.config;

import com.akshare.stock.client.StockClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AKShare客户端配置类
 */
@Configuration
public class AkShareConfig {

    /**
     * 创建StockClient Bean
     */
    @Bean
    public StockClient stockClient() {
        return new StockClient();
    }
}
