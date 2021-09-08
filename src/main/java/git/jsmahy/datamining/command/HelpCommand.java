package git.jsmahy.datamining.command;

import java.util.IllegalFormatException;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@ExecuteBefore(HelpCommand.class)
public class HelpCommand extends AbstractCommand {
    /**
     * The main constructor
     */
    HelpCommand() {
        super("h");
    }

    @Override
    void execute(String param) throws IllegalFormatException {

    }
}
