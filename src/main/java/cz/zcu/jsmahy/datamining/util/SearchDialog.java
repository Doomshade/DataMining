package cz.zcu.jsmahy.datamining.util;

import cz.zcu.jsmahy.datamining.Main;
import javafx.scene.control.TextInputDialog;

public class SearchDialog extends TextInputDialog {
    public SearchDialog(final String title, final String label) {
        setContentText(label);
        initOwner(Main.getPrimaryStage());
        setTitle(title);
        setHeaderText(null);
        setGraphic(null);
    }


}
