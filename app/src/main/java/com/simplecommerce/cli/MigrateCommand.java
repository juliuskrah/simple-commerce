package com.simplecommerce.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

/**
 * Command to run database migrations.
 *
 * @author julius.krah
 */
@CommandLine.Command(name = "migrate", description = "Run database migrations. Use --seed to also seed the database.", mixinStandardHelpOptions = true)
public class MigrateCommand implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MigrateCommand.class);
  private final Class<?> applicationClass;
  @Spec
  CommandSpec spec;

  @Option(names = {"-s", "--seed"}, description = "Seed the database after migration")
  private boolean seed;

  public MigrateCommand(Class<?> applicationClass) {
    this.applicationClass = applicationClass;
  }

  @Override
  public void run() {
    // Set profiles for migration mode
    var springBuilder = new SpringApplicationBuilder(applicationClass).profiles("migrate");
    if (seed) {
      springBuilder.profiles("seed");
      System.setProperty("simple-commerce.seeder.enabled", "true");
    }
    // Enable specific properties for migration
    System.setProperty("spring.flyway.enabled", "true");
    System.setProperty("spring.main.web-application-type", "none");
    System.setProperty("spring.jpa.hibernate.ddl-auto", "validate");

    ParseResult parseResult = spec.commandLine().getParseResult();

    ConfigurableApplicationContext context = springBuilder.run(parseResult.originalArgs().toArray(String[]::new));

    // Migration completes, close context
    context.close();

    LOG.info("Migration completed successfully {}",
        (seed ? "with seeding" : ""));
  }

}