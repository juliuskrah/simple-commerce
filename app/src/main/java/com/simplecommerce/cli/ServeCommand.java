package com.simplecommerce.cli;

import java.util.List;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;

/**
 * Command to start the GraphQL web server.
 *
 * @author julius.krah
 */
@CommandLine.Command(name = "serve", description = "Start the GraphQL web server", mixinStandardHelpOptions = true)
public class ServeCommand implements Runnable {
  @Spec
  CommandSpec spec;
  @Unmatched
  private List<String> unmatched;
  private final Class<?> applicationClass;

  public ServeCommand(Class<?> applicationClass) {
    this.applicationClass = applicationClass;
  }

  @Override
  public void run() {
    ParseResult parseResult = spec.commandLine().getParseResult();
    String[] args = new String[] {};
    if (parseResult != null) {
      // Get the original arguments from the parse result
      args = parseResult.originalArgs().toArray(String[]::new);
    }
    // Set profiles for serving mode
    ConfigurableApplicationContext context = new SpringApplicationBuilder(applicationClass).profiles("serve")
        .run(args);

    // Keep the application running
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (context.isActive()) {
        context.close();
      }
    }));

    // Wait for shutdown
    context.registerShutdownHook();
  }

}