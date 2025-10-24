package com.simplecommerce.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.grpc.client.ImportGrpcClients;

/**
 * Configuration for DexIDP authentication system integration.
 * Provides gRPC client configuration for DexIDP API.
 * 
 * @author julius.krah
 */
@Profile("oidc-authn")
@Configuration(proxyBeanMethods = false)
@ImportGrpcClients(basePackages = "com.coreos.dex.api", target = "dex-grpc")
public class DexConfiguration {

}