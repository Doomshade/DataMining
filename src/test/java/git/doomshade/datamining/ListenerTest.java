package git.doomshade.datamining;

import git.doomshade.datamining.event.CommandEvent;
import git.doomshade.datamining.event.EventHandler;
import git.doomshade.datamining.event.EventManager;

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
