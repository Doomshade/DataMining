package git.jsmahy.datamining.command;

import java.util.IllegalFormatException;

/**
 * The abstract class for commands. To register a new command use
 * {@link CommandManager#registerCommand(AbstractCommand)}
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @see CommandManager
 */
abstract class AbstractCommand {
    private final String command;

    /**
     * The main constructor
     *
     * @param command the command, i.e. the "h" in "-h"
     */
    AbstractCommand(String command) {
        this.command = command;
    }

    /**
     * Executes the command with given parameter
     *
     * @param param the command parameter
     *
     * @throws IllegalFormatException if the parameter has an invalid format
     */
    abstract void execute(String param) throws IllegalFormatException;

    String getCommand() {
        return command;
    }
}
