package com.simplecommerce;

import com.simplecommerce.cli.Command;
import com.simplecommerce.cli.MigrateCommand;
import com.simplecommerce.cli.ServeCommand;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;
import org.springframework.modulith.Modulithic;
import picocli.CommandLine;

/**
 * @since 1.0
 * @author julius.krah
 */
@Modulithic(systemName = "simple-commerce")
@SpringBootApplication(proxyBeanMethods = false)
public class SimpleCommerceApplication {

    public static void main(String[] args) {
      new CommandLine(new Command())
          .addSubcommand(new ServeCommand(SimpleCommerceApplication.class))
          .addSubcommand(new MigrateCommand(SimpleCommerceApplication.class))
          .execute(args);
    }

}
