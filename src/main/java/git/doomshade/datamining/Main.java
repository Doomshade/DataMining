package git.doomshade.datamining;

import git.doomshade.datamining.command.CommandManager;
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
        CommandManager.parseAndExecuteCommands(args);
    }

    private static void registerCommands() {
        CommandManager.registerCommand(new HelpCommand());
        CommandManager.registerCommand(new TestCommand());
    }
}
