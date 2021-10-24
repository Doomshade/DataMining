package git.doomshade.datamining;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public enum FXMLScene {
    MAIN_MENU("main-menu");

    private final String s;
    FXMLScene(final String s) {
        this.s = s;
    }

    public String getScene() {
        return s;
    }
}

