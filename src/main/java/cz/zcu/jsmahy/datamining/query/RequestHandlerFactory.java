package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;
import javafx.concurrent.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The {@link RequestHandler} factory. Here are all the registered {@link RequestHandler}s.
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerFactory {
	/**
	 * @return the DBPedia request handler
	 */
	public static RequestHandler getDBPediaRequestHandler() {
		return RequestHandlerRegistry.getDataRequestHandler(DBPediaRequestHandler.class);
	}
}
