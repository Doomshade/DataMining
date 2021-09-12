package git.doomshade.datamining.event;

import git.doomshade.datamining.command.AbstractCommand;

/**
 * A command event. This event is fired before executing the command.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public class CommandEvent extends AbstractEvent {
    private final AbstractCommand command;
    private final String param;

    public CommandEvent(AbstractCommand command, String param) {
        this.command = command;
        this.param = param;
    }

    public final AbstractCommand getCommand() {
        return command;
    }

    public final String getParam() {
        return param;
    }
}
