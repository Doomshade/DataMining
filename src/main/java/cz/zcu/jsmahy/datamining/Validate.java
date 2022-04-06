package cz.zcu.jsmahy.datamining;

import java.lang.reflect.Array;

/**
 * @author Jakub Šmrha
 * @version 1.0
 * @since 1.0
 */
public final class Validate {
	public static void validateArguments(String[] argNames, Object... arguments) throws IllegalArgumentException {
		if (arguments == null || argNames == null) {
			throw new IllegalArgumentException("Arguments cannot be null!");
		}
		if (argNames.length != arguments.length) {
			throw new RuntimeException("You must provide all names to arguments!");
		}
		_validateArguments(argNames, arguments);
	}

	private static void _validateArguments(final String[] argNames, final Object[] arguments) {
		for (int i = 0; i < arguments.length; i++) {
			validateArgument(argNames[i], arguments[i]);
		}
	}

	private static void validateArgument(final String argName, final Object argument) {
		if (argument == null) {
			throw new IllegalArgumentException(String.format("%s cannot be null!", argName));
		}
		if (argument instanceof Array) {
			for (Object o : ((Object[]) argument)) {
				validateArgument(argName, o);
			}
		}
	}
}
