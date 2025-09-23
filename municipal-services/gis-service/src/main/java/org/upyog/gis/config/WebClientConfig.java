package org.upyog.gis.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.upyog.gis.config.GisProperties;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for WFS service calls with configurable timeouts.
 * 
 * <p>This configuration provides HTTP client settings optimized for WFS service calls
 * with configurable connection and read timeouts to handle slow external services.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final GisProperties gisProperties;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tcpClient -> tcpClient
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, gisProperties.getConnectionTimeoutSeconds() * 1000)
                        .doOnConnected(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(gisProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS))));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
