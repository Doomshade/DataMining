package git.doomshade.datamining.command;

import git.doomshade.datamining.event.CommandEvent;
import git.doomshade.datamining.event.EventManager;
import git.doomshade.datamining.util.Pair;
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
    private static final Map<Character, AbstractCommand> ABBREV_COMMAND_MAP = new HashMap<>();
    private static final Map<String, AbstractCommand> NAME_COMMAND_MAP = new HashMap<>();
    private static final Map<Class<? extends AbstractCommand>, AbstractCommand> CLASS_COMMAND_MAP = new HashMap<>();
    private static final char PARAM_PREFIX = '-';
    private static final String OUTPUT_FORMAT = "%c%s ('-%c') [%s] - %s%n";
    private static final Options OPTIONS = new Options();
    private static final Map<Option, Runnable> HANDLER_LIST = new HashMap<>();

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
        /*
        if (args == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }

        // no arguments provided, show help
        if (args.length == 0) {
            getCommand(HelpCommand.class).execute(HelpCommand.ALL_COMMANDS);
            return;
        }
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid amount of arguments!");
        }

        final Collection<Pair<AbstractCommand, String>> comds = new ArrayList<>(args.length / 2);

        // increment by two in args because of command and argument
        for (int i = 0; i < args.length; i += 2) {
            final String strCmd = args[i];
            if (strCmd == null) {
                continue;
            }

            // gets rid of the "-" at the start
            final String actualCommand = strCmd.substring(strCmd.indexOf(PARAM_PREFIX));

            // look up the command
            AbstractCommand cmd = NAME_COMMAND_MAP.get(actualCommand);
            if (cmd == null) {
                cmd = ABBREV_COMMAND_MAP.get(actualCommand.charAt(0));
                if (cmd == null) {
                    throw new IllegalArgumentException(String.format("%s is an invalid command!", actualCommand));
                }
            }

            final String param = args[i + 1];
            comds.add(new Pair<>(cmd, param));
        }

        for (Pair<AbstractCommand, String> p : comds) {
            final AbstractCommand cmd = p.key;
            final String param = p.value;
            final CommandEvent event = new CommandEvent(cmd, param);
            EventManager.fireEvent(event);
            cmd.execute(param);
        }*/
    }

    /**
     * @param clazz the command class
     *
     * @return the command
     *
     * @throws IllegalStateException if the given command is not registered
     */
    public static AbstractCommand getCommand(Class<? extends AbstractCommand> clazz) throws IllegalStateException {
        final AbstractCommand cmd = CLASS_COMMAND_MAP.get(clazz);
        if (cmd == null) {
            throw new IllegalStateException("%s command is not a registered command!");
        }
        return cmd;
    }

    /**
     * Sorts the commands and the parameters based on the {@link ExecuteBefore} annotation
     *
     * @param cmds   the commands
     * @param params the parameters
     *
     * @throws IllegalArgumentException if the command length does not equal param length
     */
    private static void sort(AbstractCommand[] cmds, String[] params) throws IllegalStateException {
        if (cmds == null || params == null) {
            return;
        }
        if (cmds.length != params.length) {
            throw new IllegalArgumentException("Command and parameter length do not equal!");
        }

        // create a copy of the commands and params to a new list that will get sorted later on
        List<AbstractCommand> newCmds = new ArrayList<>(Arrays.asList(cmds));
        List<String> newParams = new LinkedList<>(Arrays.asList(params));

        // iterate in reverse order as we want to put the commands before some other command
        // say we have commands "A B C D E F" and we want to put a command "A" in front of "E", but it
        // already is that way
        for (int i = cmds.length - 1; i > 0; i--) {
            final AbstractCommand cmd = cmds[i];
            final Class<? extends AbstractCommand> clazz = cmd.getClass();

            // check if the command has an annotation, if it doesn't, leave it
            if (!clazz.isAnnotationPresent(ExecuteBefore.class)) {
                continue;
            }
            final Class<? extends AbstractCommand> executeBefore = clazz.getAnnotation(ExecuteBefore.class).value();
            if (executeBefore.equals(clazz)) {
                // probably throw some kind of warning if the command refers to itself?
                continue;
            }


            // look for the command that this command is supposed to be executed before
            for (int j = 0; j < newCmds.size(); j++) {
                AbstractCommand next = newCmds.get(j);
                if (next.getClass().equals(executeBefore)) {
                    // remove the command from the list and shift it to the new position
                    newCmds.remove(cmd);
                    newCmds.add(j, cmd);

                    // shift the params as well
                    newParams.remove(i);
                    newParams.add(j, params[j]);
                }
            }
        }
    }

    /**
     * @param command the command to search for
     *
     * @return an empty optional if the command does not exist or the command
     */
    public static Optional<AbstractCommand> getCommand(String command) {
        if (command.charAt(0) == PARAM_PREFIX) {
            command = command.substring(1);
        }
        AbstractCommand cmd = NAME_COMMAND_MAP.get(command);
        if (cmd == null) {
            cmd = ABBREV_COMMAND_MAP.get(command.charAt(0));
        }
        return cmd == null ? Optional.empty() : Optional.of(cmd);
    }

    /**
     * @return a copy of the command map
     */
    public static Collection<AbstractCommand> getCommands() {
        return List.copyOf(NAME_COMMAND_MAP.values());
    }

    /**
     * Prints the command to the console
     *
     * @param cmd the command to print to
     */
    public static void printCommand(AbstractCommand cmd) {
        System.out.printf(OUTPUT_FORMAT,
                PARAM_PREFIX,
                cmd.getCommand(),
                cmd.getAbbreviation(),
                cmd.getParameter(),
                cmd.getInfo());
    }

    /**
     * Registers all commands
     */
    public static void registerCommands() {
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
        /*registerCommand(new AppRunCommand());
        registerCommand(new DataFetchCommand());
        registerCommand(new HelpCommand());
        registerCommand(new TestCommand());*/
    }

    /**
     * Registers a command for execution
     *
     * @param cmd the command to register
     *
     * @throws IllegalArgumentException if the command is {@code null} or the command has already been registered
     */
    public static void registerCommand(AbstractCommand cmd) throws IllegalArgumentException {
        if (cmd == null) {
            throw new IllegalArgumentException("Command cannot be null!");
        }
        putIfAbsentOrThrowException(NAME_COMMAND_MAP, cmd.getCommand(), cmd,
                c -> String.format("%s has the same ID as %s!", cmd, c));
        putIfAbsentOrThrowException(CLASS_COMMAND_MAP, cmd.getClass(), cmd,
                c -> String.format("%s has already been registered!", cmd));
        putIfAbsentOrThrowException(ABBREV_COMMAND_MAP, cmd.getAbbreviation(), cmd,
                c -> String.format("%s has the same abbreviation as %s!", cmd, c));
    }

    private static <K, V> void putIfAbsentOrThrowException(Map<K, V> map, K key, V value,
                                                           Function<V, String> exceptionDescription)
            throws IllegalArgumentException {
        final V v = map.putIfAbsent(key, value);
        if (v != null) {
            throw new IllegalArgumentException(exceptionDescription.apply(v));
        }
    }
}
