package git.doomshade.datamining.data;

import git.doomshade.datamining.data.handlers.DBPediaRequestHandler;

import static git.doomshade.datamining.data.RequestHandlerRegistry.getDataRequestHandler;

/**
 * The {@link IRequestHandler} factory. Here are all the registered {@link IRequestHandler}s.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerFactory {

    /**
     * @return the DBPedia request handler
     */
    public static IRequestHandler getDBPediaRequestHandler() {
        return getDataRequestHandler(DBPediaRequestHandler.class);
    }
}
