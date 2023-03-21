package cz.zcu.jsmahy.datamining.api;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.jena.atlas.web.HttpException;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import static cz.zcu.jsmahy.datamining.api.DataNode.METADATA_KEY_NAME;
import static javafx.scene.control.Alert.AlertType.*;

public final class Alerts {
    public static void alertInvalidFileName(AtomicReference<SerializationResponse> ref, String filename, String lineName) {
        final Alert alert = new Alert(ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Soubor ''{0}'' má neplatné jméno. Přejmenujte prosím linii ''{1}''.", filename, lineName));
        final ButtonType result = alert.showAndWait()
                                       .orElse(ButtonType.OK);
        if (result == ButtonType.OK) {
            ref.set(SerializationResponse.YES);
        } else {
            ref.set(SerializationResponse.INVALID_RESPONSE);
        }
    }

    public static void alertFileExists(AtomicReference<SerializationResponse> ref, String fileName) {
        final Alert alert = new Alert(WARNING);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Přejete si přepsat ''{0}''?", fileName));
        final ObservableList<ButtonType> buttonTypes = alert.getButtonTypes();
        buttonTypes.clear();
        buttonTypes.addAll(ButtonType.YES, ButtonType.NO);
        final ButtonType result = alert.showAndWait()
                                       .orElse(ButtonType.NO);
        if (result == ButtonType.YES) {
            ref.set(SerializationResponse.YES);
        } else if (result == ButtonType.NO) {
            ref.set(SerializationResponse.NO);
        } else {
            ref.set(SerializationResponse.INVALID_RESPONSE);
        }
    }

    public static void alertExportSuccess(DataNode dataNodeRoot, File exportedFile) {
        final Alert alert = new Alert(INFORMATION);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Úspěšně exportováno ''{0}''. Soubor: ''{1}''", dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>"), exportedFile));
        alert.show();
    }

    public static void alertExportFailed(DataNode dataNodeRoot) {
        alertExportFailed(dataNodeRoot, null);
    }

    public static void alertExportFailed(DataNode dataNodeRoot, Throwable throwable) {
        final Alert alert = new Alert(ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se exportovat ''{0}''.", dataNodeRoot.getValue(METADATA_KEY_NAME, "<no name>")));
        if (throwable != null) {
            alert.setContentText(alert.getContentText()
                                      .concat(" Výjimka: \n" + throwable));
        }
        alert.show();
    }

    public static void alertCouldNotCreateFolder(File folder) {
        final Alert alert = new Alert(WARNING);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se vytvořit adresář ''{0}'' (bez výjimky)", folder));
        alert.showAndWait();
    }

    public static void alertCouldNotGetCwd(File folder) {
        final Alert alert = new Alert(ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se vybrat aktuální adresář ''{0}''. Něco je špatně...", folder));
        alert.showAndWait();
    }

    public static void alertConnectionProblems(final HttpException e) {
        final Alert alert = new Alert(ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nepodařilo se připojit ke koncovému bodu. Chyba (pro debug účely):\n{0}", e));
        alert.showAndWait();
    }

    public static void alertQueryProblems(final Throwable e) {
        final Alert alert = new Alert(ERROR);
        alert.setHeaderText("");
        alert.setContentText(MessageFormat.format("Nastala chyba při tvorbě linie. Chyba (pro debug účely):\n{0}", e));
        alert.showAndWait();
    }
}
