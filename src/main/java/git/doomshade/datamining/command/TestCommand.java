package git.doomshade.datamining.command;

import java.util.IllegalFormatException;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
@ExecuteBefore(HelpCommand.class)
public class TestCommand extends AbstractCommand {
    /**
     * The main constructor
     */
    public TestCommand() {
        super("test", 't', "any string", "test command");
    }

    @Override
    protected void execute(String param) throws IllegalFormatException {

    }
}
