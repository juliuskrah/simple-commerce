package com.simplecommerce.shared.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.aspectj.AnnotationTransactionAspect;
import org.springframework.transaction.config.TransactionManagementConfigUtils;

/**
 * @author julius.krah
 */
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Configuration(proxyBeanMethods = false)
class TransactionConfiguration {

  @Bean(name = TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  AnnotationTransactionAspect transactionAspect(TransactionManager txManager) {
    AnnotationTransactionAspect txAspect = AnnotationTransactionAspect.aspectOf();
    txAspect.setTransactionManager(txManager);
    return txAspect;
  }
}

