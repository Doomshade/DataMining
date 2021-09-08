package git.doomshade.datamining.command;

import java.util.*;
import java.util.function.Function;

/**
 * The command manager
 * <p>
 * A command is in format "-{@literal <cmd>} {@literal <param>}". If the {@literal <param>} is empty, there may be
 * odd number of arguments
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

    /**
     * Attempts to parse and execute commands based on the arguments provided from the CLI
     *
     * @param args the CLI arguments
     *
     * @throws IllegalArgumentException if the amount of arguments is invalid
     * @throws IllegalFormatException   when a command has a parameter with invalid formatting
     */
    public static void parseAndExecuteCommands(String[] args) throws IllegalArgumentException, IllegalFormatException {
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

        // the length has to be divisible by two otherwise an IllegalArgumentException is thrown
        final int len = args.length / 2;
        final AbstractCommand[] cmds = new AbstractCommand[len];
        final String[] params = new String[len];

        for (int i = 0; i < args.length; ) {
            AbstractCommand cmd = NAME_COMMAND_MAP.get(args[i].substring(1));
            if (cmd == null) {
                cmd = ABBREV_COMMAND_MAP.get(args[i].charAt(1));
                if (cmd == null) {
                    throw new IllegalArgumentException(String.format("%s is an invalid command!", args[i]));
                }
            }
            cmds[i / 2] = cmd;
            params[i / 2] = args[i + 1];
            i += 2;
        }

        for (int i = 0; i < cmds.length; i++) {
            cmds[i].execute(params[i]);
        }
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
     * @throws IllegalStateException if the command length does not equal param length
     */
    private static void sort(AbstractCommand[] cmds, String[] params) throws IllegalStateException {
        if (cmds == null || params == null) {
            return;
        }
        if (cmds.length != params.length) {
            throw new IllegalStateException("Command and parameter length do not equal!");
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
}
