package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The registry for {@link RequestHandler}s. To register a {@link RequestHandler} use
 * {@link RequestHandlerRegistry#register(RequestHandler)}
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerRegistry {
	private static final Map<Class<? extends RequestHandler>, RequestHandler> HANDLERS = new LinkedHashMap<>();

	static {
		register(new DBPediaRequestHandler());
	}

	/**
	 * Registers a data request handler
	 *
	 * @param handler the handler to register
	 */
	public static void register(RequestHandler handler) {
		HANDLERS.putIfAbsent(handler.getClass(), handler);
	}

	/**
	 * @param handlerClass the handler class
	 *
	 * @return the handler for data requests
	 *
	 * @throws IllegalArgumentException if the data handler has not been registered yet
	 * @see RequestHandlerRegistry#register(RequestHandler)
	 * @see RequestHandlerFactory
	 */
	public static RequestHandler getDataRequestHandler(Class<? extends RequestHandler> handlerClass) throws
	                                                                                                 IllegalArgumentException {
		RequestHandler handler = HANDLERS.get(handlerClass);
		if (handler == null) {
			throw new IllegalArgumentException(String.format("%s is not a registered handler!",
			                                                 handlerClass.getSimpleName()
			));
		}
		return handler;
	}
}
