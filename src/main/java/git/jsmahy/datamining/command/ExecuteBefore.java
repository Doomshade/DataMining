package git.jsmahy.datamining.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteBefore {
    Class<? extends AbstractCommand> value();
}
