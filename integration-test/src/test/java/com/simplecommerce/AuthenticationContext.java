package com.simplecommerce;

/**
 * Holds authentication context information for connecting to a service.
 *
 * @param hostname     the hostname of the service
 * @param port         the port of the service
 * @param clientId     the client ID for authentication
 * @param clientSecret the client secret for authentication
 * @param username     the username for authentication
 * @param password     the password for authentication
 * @author julius.krah
 */
public record AuthenticationContext(
    String hostname,
    int port,
    String clientId,
    String clientSecret,
    String username,
    String password
) {

}
