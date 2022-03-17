package cz.zcu.jsmahy.datamining.command;

import org.apache.commons.cli.*;

import java.util.*;
import java.util.function.Function;

/**
 * The command manager
 * <p>
 * A command is in format "-{@literal <cmd>} {@literal <param>}". If the {@literal <param>} is empty, there may be odd
 * number of arguments
 * <p>
 * A list of valid commands (the parameters are in parentheses):<br>
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
public final class CommandManager {
    private static final Options OPTIONS = new Options();
    private static final Map<Option, Runnable> HANDLER_LIST = new HashMap<>();

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
    public static void parseAndExecuteCommands(String[] args)
            throws IllegalArgumentException, IllegalFormatException, ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine c = parser.parse(OPTIONS, args);
        for (Option o : c.getOptions()) {
            final Runnable r = HANDLER_LIST.get(o);
            if (r != null) {
                r.run();
            }
        }
    }

    /**
     * Registers all commands
     */
    private static void registerCommands() {
        Option appRun = new Option("s", "start", false, "Runs the app");
        HANDLER_LIST.put(appRun, () -> System.out.println("RUN"));
        Option dataFetch = new Option("d", "data", true, "Fetches data");
        HANDLER_LIST.put(dataFetch, () -> System.out.println("DATA"));
        Option help = new Option("h", "help", true, "shows help");
        HANDLER_LIST.put(help, () -> System.out.println("HELP"));
        Option test = new Option("t", "test", true, "test");
        HANDLER_LIST.put(test, () -> System.out.println("TEST"));

        OPTIONS.addOption(appRun)
                .addOption(dataFetch)
                .addOption(help)
                .addOption(test);
    }
}
