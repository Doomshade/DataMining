package cz.zcu.jsmahy.datamining.query;

import cz.zcu.jsmahy.datamining.query.handlers.DBPediaRequestHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The registry for {@link IRequestHandler}s. To register a {@link IRequestHandler} use
 * {@link RequestHandlerRegistry#register(IRequestHandler)}
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class RequestHandlerRegistry {
	private static final Map<Class<? extends IRequestHandler>, IRequestHandler> HANDLERS = new LinkedHashMap<>();

	static {
		register(new DBPediaRequestHandler());
	}

	/**
	 * Registers a data request handler
	 *
	 * @param handler the handler to register
	 */
	public static void register(IRequestHandler handler) {
		HANDLERS.putIfAbsent(handler.getClass(), handler);
	}

	/**
	 * @param handlerClass the handler class
	 *
	 * @return the handler for data requests
	 *
	 * @throws IllegalArgumentException if the data handler has not been registered yet
	 * @see RequestHandlerRegistry#register(IRequestHandler)
	 * @see RequestHandlerFactory
	 */
	public static IRequestHandler getDataRequestHandler(Class<? extends IRequestHandler> handlerClass) throws
	                                                                                                   IllegalArgumentException {
		IRequestHandler handler = HANDLERS.get(handlerClass);
		if (handler == null) {
			throw new IllegalArgumentException(String.format("%s is not a registered handler!",
			                                                 handlerClass.getSimpleName()
			));
		}
		return handler;
	}
}
