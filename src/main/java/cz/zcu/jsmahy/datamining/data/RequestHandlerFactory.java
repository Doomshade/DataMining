package cz.zcu.jsmahy.datamining.data;

import cz.zcu.jsmahy.datamining.data.handlers.DBPediaRequestHandler;

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
        return RequestHandlerRegistry.getDataRequestHandler(DBPediaRequestHandler.class);
    }
}
