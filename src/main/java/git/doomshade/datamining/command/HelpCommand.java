package git.doomshade.datamining.command;

import java.util.IllegalFormatException;
import java.util.IllegalFormatFlagsException;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class HelpCommand extends AbstractCommand {
    public static final String ALL_COMMANDS = "all";

    /**
     * The main constructor
     */
    public HelpCommand() {
        super("help", 'h', "all/<command_name>", "shows command info");
    }

    @Override
    protected void execute(String param) throws IllegalFormatException {
        if (param.isEmpty() || param.equalsIgnoreCase(ALL_COMMANDS)) {
            System.out.println("All known commands:");
            for (AbstractCommand cmd : CommandManager.getCommands()) {
                CommandManager.printCommand(cmd);
            }
            return;
        }

        // print the command info if the command exists, print error otherwise
        CommandManager.getCommand(param).ifPresentOrElse(CommandManager::printCommand, () -> printError(param));
    }

    private static void printError(String cmd) throws IllegalFormatException {
        throw new IllegalFormatFlagsException(String.format("Command %s does not exist!", cmd));
    }
}
