package com.simplecommerce.junit;

import static com.simplecommerce.BaseDockerComposeTest.DEX_IDP_PORT;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.ReflectionSupport.streamFields;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.simplecommerce.AuthenticationContext;
import com.simplecommerce.AuthenticationContextHolder;
import com.simplecommerce.AuthenticationUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;

public class AccessTokenExtension implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback {

  public static final String DEX_SERVICE_NAME = "oidc-1";
  private static final Namespace SIMPLE_COMMERCE = create("org.simple.commerce");
  private static final String COMPOSE_CONTAINER = ComposeContainer.class.getName();
  private static final String CLIENT_ID = "simple-commerce";
  private static final String CLIENT_SECRET = "Zm9vYmFy";

  private static Predicate<Field> isContainer() {
    return field -> {
      boolean isAnnotatedWithContainer = isAnnotated(field, Container.class);
      if (isAnnotatedWithContainer) {
        return ComposeContainer.class.isAssignableFrom(field.getType());
      }
      return false;
    };
  }

  private Predicate<Field> isSharedContainer() {
    return isContainer().and(ModifierSupport::isStatic);
  }

  @Nullable
  private ComposeContainer getContainerInstance(Field field) {
    try {
      field.setAccessible(true);
      return (ComposeContainer) field.get(null);
    } catch (IllegalAccessException _) {
      return null;
    }
  }

  @Nullable
  private Login toLogin(Actor actor) {
    JsonMapper jsonFactory = JsonMapper.builder().build();
    var resource = new ClassPathResource("auth/actors.json");
    try (var parser = jsonFactory.createParser(resource.getInputStream())) {
      ObjectNode node = parser.readValueAsTree();
      var user = node.get(actor.value());
      var password = (TextNode) user.path("password");
      var email = (TextNode) user.path("email");
      return new Login(email.asText(), password.asText());
    } catch (IOException _) {
      return null;
    }
  }

  private record Login(String email, String password) {

  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.isAnnotated(Actor.class);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    ExtensionContext rootContext = extensionContext.getRoot();
    Store store = rootContext.getStore(SIMPLE_COMMERCE);
    Optional<Actor> actorAnnotation = parameterContext.findAnnotation(Actor.class);
    var login = toLogin(actorAnnotation.get());
    var composeContainer = extensionContext.getStore(SIMPLE_COMMERCE).remove(COMPOSE_CONTAINER, ComposeContainer.class);
    return store.getOrComputeIfAbsent(login, key -> {
      var hostname = composeContainer.getServiceHost(DEX_SERVICE_NAME, DEX_IDP_PORT);
      var port = composeContainer.getServicePort(DEX_SERVICE_NAME, DEX_IDP_PORT);
      var context = new AuthenticationContext(hostname, port, CLIENT_ID, CLIENT_SECRET, key.email, key.password);
      return AuthenticationContextHolder.withContext(context, AuthenticationUtils::getAccessToken);
    }, String.class);
  }

  @Override
  public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
    return ExtensionContextScope.TEST_METHOD;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public void beforeTestExecution(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    Stream<Field> fieldStream = streamFields(testClass, isSharedContainer(), HierarchyTraversalMode.TOP_DOWN);
    var composeContainer = fieldStream.map(this::getContainerInstance).findFirst();
    context.getStore(SIMPLE_COMMERCE).put(COMPOSE_CONTAINER, composeContainer.get());
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    // NO-OP
  }
}
