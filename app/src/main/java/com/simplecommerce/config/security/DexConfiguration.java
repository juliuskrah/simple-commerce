package com.simplecommerce.config.security;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for DexIDP authentication system integration.
 * Provides gRPC client configuration for DexIDP API.
 * 
 * @author julius.krah
 */
@Configuration(proxyBeanMethods = false)
@Profile("oidc-authn")
@EnableConfigurationProperties(DexConfiguration.DexProperties.class)
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "servlet")
public class DexConfiguration {

    @ConfigurationProperties(prefix = "dex")
    public static class DexProperties {
        private String host = "localhost";
        private int grpcPort = 5557;
        private boolean usePlaintext = true;
        private String issuer = "http://127.0.0.1:5556/dex";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getGrpcPort() {
            return grpcPort;
        }

        public void setGrpcPort(int grpcPort) {
            this.grpcPort = grpcPort;
        }

        public boolean isUsePlaintext() {
            return usePlaintext;
        }

        public void setUsePlaintext(boolean usePlaintext) {
            this.usePlaintext = usePlaintext;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    @Bean(name = "dexGrpcChannel")
    public ManagedChannel dexGrpcChannel(DexProperties properties) {
        var builder = ManagedChannelBuilder.forAddress(properties.getHost(), properties.getGrpcPort());
        
        if (properties.isUsePlaintext()) {
            builder.usePlaintext();
        }
        
        return builder.build();
    }
}