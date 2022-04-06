package cz.zcu.jsmahy.datamining.command;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.function.Consumer;

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
	private static final Options                         OPTIONS      = new Options();
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
		CommandLineParser parser = new DefaultParser();
		CommandLine c = parser.parse(OPTIONS, args);
		for (Option o : c.getOptions()) {
			final Consumer<String[]> r = HANDLER_LIST.get(o);
			if (r != null) {
				r.accept(o.getValues());
			}
		}
	}

	/**
	 * Registers all commands
	 */
	private static void registerCommands() {
		HANDLER_LIST.put(new Option("s", "start", false, "Runs the app"), CommandManager::handleAppRun);
		HANDLER_LIST.put(new Option("d", "data", true, "Fetches data"), CommandManager::handleDataFetch);
		HANDLER_LIST.put(new Option("h", "help", true, "shows help"), CommandManager::handleHelp);
		HANDLER_LIST.put(new Option("t", "test", true, "test"), CommandManager::handleTest);

		for (Option opt : HANDLER_LIST.keySet()) {
			OPTIONS.addOption(opt);
		}
	}


	private static void handleAppRun(String[] args) {
		System.out.println("RUN");
	}

	private static void handleDataFetch(String[] args) {
		System.out.println("DATA");
	}

	private static void handleHelp(String[] args) {
		System.out.println("HELP");
	}

	private static void handleTest(String[] args) {
		System.out.println("TEST");
	}

}
