package git.doomshade.datamining.command;

import git.doomshade.datamining.app.App;

import java.util.IllegalFormatException;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class AppRunCommand extends AbstractCommand {
    public AppRunCommand() throws IllegalArgumentException {
        super("start", 's', NO_PARAMETER, "Runs the app");
    }

    @Override
    protected void execute(String param) throws IllegalFormatException {
        App.exec(param);
    }
}
