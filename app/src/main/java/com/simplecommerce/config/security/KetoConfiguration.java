package com.simplecommerce.config.security;

import com.simplecommerce.config.security.KetoConfiguration.KetoProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for Ory Keto authorization system integration.
 * Provides gRPC client configuration for Keto Read and Write APIs.
 * 
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@Profile("keto-authz")
@EnableConfigurationProperties(KetoProperties.class)
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "servlet")
public class KetoConfiguration {

    @ConfigurationProperties(prefix = "keto")
    public static class KetoProperties {
        private String host = "localhost";
        private int readPort = 4466;
        private int writePort = 4467;
        private boolean usePlaintext = true;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getReadPort() {
            return readPort;
        }

        public void setReadPort(int readPort) {
            this.readPort = readPort;
        }

        public int getWritePort() {
            return writePort;
        }

        public void setWritePort(int writePort) {
            this.writePort = writePort;
        }

        public boolean isUsePlaintext() {
            return usePlaintext;
        }

        public void setUsePlaintext(boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
        }
    }

    @Bean(name = "ketoReadChannel")
    public ManagedChannel ketoReadChannel(KetoProperties properties) {
        var builder = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getReadPort());
        
        if (properties.isUsePlaintext()) {
            builder.usePlaintext();
        }
        
        return builder.build();
    }

    @Bean(name = "ketoWriteChannel")
    public ManagedChannel ketoWriteChannel(KetoProperties properties) {
        var builder = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getWritePort());
        
        if (properties.isUsePlaintext()) {
            builder.usePlaintext();
        }
        
        return builder.build();
    }
}