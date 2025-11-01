package com.simplecommerce.shared.authentication;

import com.coreos.dex.api.Api.CreatePasswordReq;
import com.coreos.dex.api.Api.ListPasswordReq;
import com.coreos.dex.api.Api.Password;
import com.coreos.dex.api.Api.VerifyPasswordReq;
import com.coreos.dex.api.DexGrpc.DexBlockingStub;
import com.google.protobuf.ByteString;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author julius.krah
 */
@Service
@Profile("oidc-authn")
public class DexIdpService {

  private static final Logger LOG = LoggerFactory.getLogger(DexIdpService.class);
  private static final Supplier<PasswordEncoder> PASSWORD_ENCODER = StableValue.supplier(BCryptPasswordEncoder::new);
  private final DexBlockingStub dexService;

  private PasswordEncoder getPasswordEncoder() {
    return PASSWORD_ENCODER.get();
  }

  public DexIdpService(DexBlockingStub dexService) {
    this.dexService = dexService;
  }

  /// @param email the email of the user
  /// @param username the username of the user
  /// @param rawPassword the plain password of the user
  /// @return false when a new user is created otherwise true
  public boolean addUser(String email, String username, CharSequence rawPassword) {
    LOG.debug("Adding {} / {}", username, email);
    var password = getPasswordEncoder().encode(rawPassword);
    var response = dexService.createPassword(CreatePasswordReq.newBuilder()
        .setPassword(Password.newBuilder()
            .setUserId(UUID.randomUUID().toString())
            .setEmail(email)
            .setUsername(username)
            .setHash(ByteString.copyFromUtf8(password)))
        .build());
    return response.getAlreadyExists();
  }

  /// @return list of users
  public List<User> listUsers() {
    LOG.debug("Listing users");
    var users = dexService.listPasswords(ListPasswordReq.newBuilder().build());
    return users.getPasswordsList().stream().map(password ->
        new User(password.getUserId(), password.getUsername(), password.getEmail())
    ).toList();
  }

  /// @param email the email of the user
  /// @param rawPassword the plain password of the user
  /// @return true is the provided credentials is valid
  public boolean verifyLogin(String email, String rawPassword) {
    LOG.debug("Verifying {}", email);
    var response = dexService.verifyPassword(VerifyPasswordReq.newBuilder()
        .setEmail(email).setPassword(rawPassword)
        .build());
    return response.getVerified();
  }
}
