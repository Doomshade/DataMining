package cz.zcu.jsmahy.datamining;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public enum FXMLScene {
    MAIN_MENU("main-menu"),
    IB_TEMPLATE_MENU("ib-template");

    private final String s;
    FXMLScene(final String s) {
        this.s = s;
    }

    public String getScene() {
        return s;
    }
}

