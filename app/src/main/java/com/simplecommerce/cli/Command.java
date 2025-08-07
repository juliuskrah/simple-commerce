package com.simplecommerce.cli;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Base interface for CLI commands.
 * 
 * @author julius.krah
 */
@CommandLine.Command(name = "commerce", mixinStandardHelpOptions = true)
public class Command implements Runnable {
  @Spec
  CommandSpec spec;

  @Override
  public void run() {
    ServeCommand command = spec.subcommands().get("serve").getCommand();
    command.run();
  }
}