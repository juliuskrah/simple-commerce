package com.simplecommerce.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.grpc.client.ImportGrpcClients;

/**
 * Configuration for Ory Keto authorization system integration.
 * Provides gRPC client configuration for Keto Read and Write APIs.
 * 
 * @author julius.krah
 */
@Profile("keto-authz")
@Configuration(proxyBeanMethods = false)
@ImportGrpcClients(basePackages = "sh.ory.keto.relation_tuples.v1alpha2", target = "keto-read")
@ImportGrpcClients(basePackages = "sh.ory.keto.opl.v1alpha1", target = "keto-opl")
public class KetoConfiguration {

}