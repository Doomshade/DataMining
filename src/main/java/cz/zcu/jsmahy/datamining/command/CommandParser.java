package cz.zcu.jsmahy.datamining.command;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <p>The command parser</p>
 * <p>A command is in format "-{@literal <cmd>} {@literal <param>}". If the {@literal <param>} is empty, there may be odd
 * number of arguments</p>
 * <p>A list of valid commands (the parameters are in parentheses):</p>
 * <ul>
 *     <li>-run -h all -t test&#9(all, test)</li>
 *     <li>-run -h -all -t test&#9(-all, test)</li>
 *     <li>-run -h all -t -test&#9(all, -test)</li>
 *     <li>-run -h -all -t -test&#9(-all, -test)</li>
 * </ul>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class CommandParser {
    private static final Options OPTIONS = new Options();
    private static final Map<Option, Consumer<String[]>> HANDLER_LIST = new HashMap<>();

    static {
        registerCommands();
    }

    /**
     * Attempts to parse and execute commands based on the arguments provided from the CLI
     *
     * @param args the CLI arguments
     *
     * @throws IllegalArgumentException if the amount of arguments is invalid
     * @throws IllegalFormatException   when a command has a parameter with invalid formatting
     */
    public static void parseAndExecuteCommands(String[] args) throws
                                                              IllegalArgumentException,
                                                              IllegalFormatException,
                                                              ParseException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmdLine = parser.parse(OPTIONS, args);
        for (final Option opt : cmdLine.getOptions()) {
            final Consumer<String[]> r = HANDLER_LIST.get(opt);
            if (r != null) {
                r.accept(opt.getValues());
            }
        }
    }

    /**
     * Registers all commands
     */
    private static void registerCommands() {
        HANDLER_LIST.put(new Option("s", "start", false, "Runs the app"), CommandParser::handleAppRun);
        HANDLER_LIST.put(new Option("d", "data", true, "Fetches data"), CommandParser::handleDataFetch);
        HANDLER_LIST.put(new Option("h", "help", true, "Shows help"), CommandParser::handleHelp);
        HANDLER_LIST.put(new Option("t", "test", true, "test"), CommandParser::handleTest);

        for (final Option opt : HANDLER_LIST.keySet()) {
            OPTIONS.addOption(opt);
        }
    }

    // TODO: implement handlers
    private static void handleAppRun(String[] args) {
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static void handleDataFetch(String[] args) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static void handleHelp(String[] args) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static void handleTest(String[] args) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
