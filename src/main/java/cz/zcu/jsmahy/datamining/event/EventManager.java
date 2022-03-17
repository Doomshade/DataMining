package cz.zcu.jsmahy.datamining.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The event manager
 *
 * @author Jakub Šmrha
 * @version 1.0
 * @see AbstractEvent
 */
public class EventManager {
    // we map events to the listeners, this reduces the search by a lot
    private static final Map<Class<? extends AbstractEvent>, Collection<Object>> EVENT_HANDLERS =
            new LinkedHashMap<>();

    /**
     * Fires an event for listeners to listen to
     *
     * @param event the event
     */
    public static void fireEvent(AbstractEvent event) {
        Collection<Object> listeners = EVENT_HANDLERS.get(event.getClass());

        // no listeners listen to that event
        if (listeners == null) {
            return;
        }

        for (Object listener : listeners) {
            for (Method m : listener.getClass().getDeclaredMethods()) {
                // check for annotated methods only
                if (!isEventMethod(m)) {
                    continue;
                }
                // the event must be the instance of the fired event
                if (!m.getParameters()[0].getType()
                        .isInstance(event)) {
                    continue;
                }
                try {
                    m.setAccessible(true);
                    m.invoke(listener, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // TODO add logging
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Checks whether the method is a viable event method
     *
     * @param m the method to check for
     *
     * @return {@code true} if the method is annotated with {@link EventHandler} and the first
     * parameter is {@link
     * AbstractEvent}
     */
    private static boolean isEventMethod(Method m) {
        return m.isAnnotationPresent(EventHandler.class) // check for annotated methods only
                && m.getParameterCount() >= 1 // the method must have a parameter
                && AbstractEvent.class.isAssignableFrom(m.getParameters()[0].getType());

    }

    /**
     * Registers event for a given listener
     *
     * @param listener the listener
     */
    public static void registerEvents(Object listener) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            if (!isEventMethod(m)) {
                continue;
            }
            @SuppressWarnings("all") final Class<? extends AbstractEvent> eventClass =
                    (Class<? extends AbstractEvent>) m.getParameters()[0].getType();
            Collection<Object> listeners = EVENT_HANDLERS.getOrDefault(eventClass, new HashSet<>());
            listeners.add(listener);
            EVENT_HANDLERS.putIfAbsent(eventClass, listeners);
        }
    }
}
