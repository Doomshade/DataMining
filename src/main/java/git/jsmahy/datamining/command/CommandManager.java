package git.jsmahy.datamining.command;

import java.util.*;

/**
 * The command manager
 *
 * @author Doomshade
 * @version 1.0
 */
public final class CommandManager {
    private static final Map<String, AbstractCommand> COMMAND_MAP = new HashMap<>();

    /**
     * Registers a command for execution
     *
     * @param cmd the command to register
     *
     * @throws IllegalArgumentException if the command is {@code null}
     */
    public static void registerCommand(AbstractCommand cmd) throws IllegalArgumentException {
        if (cmd == null) {
            throw new IllegalArgumentException("Command cannot be null!");
        }
        COMMAND_MAP.putIfAbsent(cmd.getCommand(), cmd);
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
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid amount of arguments!");
        }

        // the length has to be divisible by two otherwise an IllegalArgumentException is thrown
        final int len = args.length / 2;
        final AbstractCommand[] cmds = new AbstractCommand[len];
        final String[] params = new String[len];

        for (int i = 0; i < args.length; i += 2) {
            final AbstractCommand cmd = COMMAND_MAP.get(args[i]);
            if (cmd == null) {
                throw new IllegalArgumentException(String.format("%s is an invalid command!", args[i]));
            }
            cmds[i / 2] = cmd;
            params[i / 2] = args[i + 1];
        }

        // now sort the commands based on the ExecuteBefore annotation
        sort(cmds, params);
    }

    private static void sort(AbstractCommand[] cmds, String[] params) throws IllegalStateException {
        if (cmds.length != params.length) {
            throw new IllegalStateException("Command and parameter length do not equal!");
        }

        // create a copy of the commands to a new list that will get sorted later on
        LinkedList<AbstractCommand> newCmds = new LinkedList<>(Arrays.asList(cmds));

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
            final ListIterator<AbstractCommand> it = newCmds.listIterator();

            while (it.hasNext()) {
                final AbstractCommand next = it.next();
                if (next.getClass().equals(executeBefore)) {
                    // remove the command from the list and shift it to the new position
                    newCmds.remove(cmd);
                    newCmds.add(it.previousIndex(), cmd);
                }
            }
        }
    }

    /**
     * @return a copy of the command map
     */
    public static Map<String, AbstractCommand> getCommands() {
        return Map.copyOf(COMMAND_MAP);
    }
}
