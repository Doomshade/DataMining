package cz.zcu.jsmahy.datamining;

/**
 * @author Jakub Šmrha
 * @version 1.0
 */
public enum FXMLScene {
	MAIN("app/controller/main"),
	MAIN_MENU("app/controller/main-menu"),
	IB_TEMPLATE_MENU("app/controller/ib-template");

	private final String s;

	FXMLScene(final String s) {
		this.s = s;
	}

	public String getScenePath() {
		return s;
	}
}
