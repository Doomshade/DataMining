package git.doomshade.datamining;

import git.doomshade.datamining.command.HelpCommand;
import git.doomshade.datamining.command.TestCommand;

import static git.doomshade.datamining.command.CommandManager.parseAndExecuteCommands;
import static git.doomshade.datamining.command.CommandManager.registerCommand;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {
        registerCommands();
        parseAndExecuteCommands(args);
    }

    private static void registerCommands() {
        registerCommand(new HelpCommand());
        registerCommand(new TestCommand());
    }
}
