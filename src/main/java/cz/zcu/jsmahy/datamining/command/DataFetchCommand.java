package cz.zcu.jsmahy.datamining.command;

import java.util.IllegalFormatException;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class DataFetchCommand extends AbstractCommand {
    public DataFetchCommand() throws IllegalArgumentException {
        super("fetch", 'f', "resource", "Fetches the data");
    }

    @Override
    protected void execute(String param) throws IllegalFormatException {
    }
}
