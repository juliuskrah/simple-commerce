/**
 * The payment module handles payment processing and transactions.
 *
 * <p>This module provides functionality for:
 * <ul>
 *   <li>Payment processing and authorization</li>
 *   <li>Payment capture and settlement</li>
 *   <li>Refund processing</li>
 *   <li>Integration with payment providers (Stripe, PayPal, etc.)</li>
 * </ul>
 *
 * @author julius.krah
 * @since 1.0
 */
@ApplicationModule(
    displayName = "Payment Management",
    allowedDependencies = {"order", "order :: *", "shared", "shared :: *"}
)
package com.simplecommerce.payment;

import org.springframework.modulith.ApplicationModule;
