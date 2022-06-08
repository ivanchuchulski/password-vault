package password.vault.client;

import javafx.application.Application;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import password.vault.client.communication.Client;
import password.vault.client.console.ConsoleClient;
import password.vault.client.gui.GUIClient;

public class Main {
    public static void main(String[] args) {
        boolean consoleMode = determineConsoleMode(args);

        Client client = new Client();
        if (consoleMode) {
            ConsoleClient consoleClient = new ConsoleClient(client);
            consoleClient.run();
        } else {
            GUIClient.setClient(client);
            Application.launch(GUIClient.class);
        }
    }

    private static boolean determineConsoleMode(String[] args) {
        try {
            Option consoleOption = Option.builder("c")
                                         .longOpt("console")
                                         .hasArg(true)
                                         .argName("true|false")
                                         .type(Boolean.TYPE)
                                         .desc("launch program in a console mode only")
                                         .build();

            Option helpOption = Option.builder("h").longOpt("help").hasArg(false).build();

            Options options = new Options();
            options.addOption(consoleOption);
            options.addOption(helpOption);

            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);

            HelpFormatter formatter = new HelpFormatter();

            if (commandLine.hasOption(consoleOption)) {
                String optionValue = commandLine.getOptionValue(consoleOption);
                return Boolean.parseBoolean(optionValue);
            } else if (commandLine.hasOption(helpOption)) {
                formatter.printHelp(".\\gradlew :client:run [options]", options);
                System.exit(0);
                return false;
            } else {
                return false;
            }
        } catch (ParseException e) {
            throw new RuntimeException("unable to parse cli arguments", e);
        }
    }
}
