package cz.zcu.jsmahy.datamining;

import cz.zcu.jsmahy.datamining.event.CommandEvent;
import cz.zcu.jsmahy.datamining.event.EventHandler;
import cz.zcu.jsmahy.datamining.event.EventManager;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public class ListenerTest {
    public static void main(String[] args) {
        EventManager.registerEvents(new ListenerTest());
        EventManager.fireEvent(new CommandEvent(null, "no param"));
    }

    @EventHandler
    public void tst(CommandEvent e) {
        System.out.println("TEST WORCED - " + e.getParam());
    }
}
