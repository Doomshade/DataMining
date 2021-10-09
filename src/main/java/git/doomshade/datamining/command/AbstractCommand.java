package git.doomshade.datamining.command;

import java.util.IllegalFormatException;

/**
 * The abstract class for commands. To register a new command use
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @see CommandManager
 */
public abstract class AbstractCommand {
    public static final char NO_ABBREVIATION = ' ';
    public static final String NO_PARAMETER = "";
    public static final String NO_INFO = "";
    private final String command;
    private final char abbrev;
    private final String info;
    private final String param;

    /**
     * Calls {@link AbstractCommand#AbstractCommand(String, char)} with no abbreviation
     *
     * @see AbstractCommand#AbstractCommand(String, char)
     */
    public AbstractCommand(String command) {
        this(command, NO_ABBREVIATION);
    }

    /**
     * Calls {@link AbstractCommand#AbstractCommand(String, char, String)} with no parameter
     *
     * @see AbstractCommand#AbstractCommand(String, char, String)
     */
    public AbstractCommand(String command, char abbrev) {
        this(command, abbrev, NO_PARAMETER);
    }

    /**
     * Calls {@link AbstractCommand#AbstractCommand(String, char, String, String)} with no info
     *
     * @see AbstractCommand#AbstractCommand(String, char, String, String)
     */
    public AbstractCommand(String command, char abbrev, String param) {
        this(command, abbrev, param, NO_INFO);
    }

    /**
     * The main constructor
     *
     * @param command the command
     * @param abbrev  i.e. the "h" in "-h"
     * @param param   the parameter
     * @param info    the info about this command
     *
     * @throws IllegalArgumentException if command is {@code null} or empty
     */
    public AbstractCommand(String command, char abbrev, String param, String info) throws IllegalArgumentException {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException(String.format("Command cannot be null or empty! (empty = %s)",
                    command != null));
        }
        this.command = command;
        this.abbrev = abbrev;
        this.param = checkParam(param);
        this.info = checkInfo(info);
    }

    private String checkInfo(String info) {
        return info == null ? NO_INFO : info;
    }

    private String checkParam(String param) {
        return param == null ? NO_PARAMETER : param;
    }

    /**
     * Executes the command with given parameter
     *
     * @param param the command parameter
     *
     * @throws IllegalFormatException if the parameter has an invalid format
     */
    protected abstract void execute(String param) throws IllegalFormatException;

    final String getCommand() {
        return command;
    }

    final char getAbbreviation() {
        return abbrev;
    }

    final String getInfo() {
        return info;
    }

    final String getParameter() {
        return param;
    }

    @Override
    public String toString() {
        return "AbstractCommand{" +
                "command='" + command + '\'' +
                ", abbrev=" + abbrev +
                ", info='" + info + '\'' +
                ", param='" + param + '\'' +
                '}';
    }
}
