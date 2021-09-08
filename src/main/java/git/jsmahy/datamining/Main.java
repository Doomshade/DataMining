package git.jsmahy.datamining;

import git.jsmahy.datamining.command.HelpCommand;
import git.jsmahy.datamining.command.TestCommand;

import static git.jsmahy.datamining.command.CommandManager.parseAndExecuteCommands;
import static git.jsmahy.datamining.command.CommandManager.registerCommand;

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
