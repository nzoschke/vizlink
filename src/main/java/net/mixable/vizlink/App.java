package net.mixable.vizlink;

import java.io.IOException;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) throws IOException, InterruptedException, ParseException {
    Options options = new Options();
    options.addOption("h", "help", false, "help");
    options.addOption("n", "number", true, "virtual CDJ player number (default 7)");
    options.addOption("r", "rpc", true, "RPC");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption("h")) {
      new HelpFormatter().printHelp("vizlink", options);
      return;
    }

    int number = Integer.parseInt(coalesce(cmd.getOptionValue("n"), "7"));

    IO io = new IO(out -> {
      System.out.println(out);
    });

    if (cmd.hasOption("r")) {
      VizLink.rpc(io, cmd.getOptionValue("r"));
      System.exit(0);
    }

    stdioPipe(io);
    VizLink.start(io, number);

    while (true) {
      try {
        Thread.sleep(60000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static void stdioPipe(IO io) {
    new Thread(() -> {
      System.err.println("stdioPipe");
      try (Scanner scanner = new Scanner(System.in)) {
        while (true) {
          String line = scanner.nextLine();
          io.in(line);
        }
      }
    }).start();
  }

  public static String coalesce(String val, String def) {
    if (val == null || val == "") {
      return def;
    }
    return val;
  }
}
