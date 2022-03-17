package cz.zcu.jsmahy.datamining.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to annotate a command that is supposed to be executed before some other command
 * <p>
 * <b>NOT YET IMPLEMENTED</b>
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteBefore {
    Class<? extends AbstractCommand> value();
}
