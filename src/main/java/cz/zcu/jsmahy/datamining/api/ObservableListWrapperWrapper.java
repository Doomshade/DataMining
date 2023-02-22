package cz.zcu.jsmahy.datamining.api;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.FXCollections;

public class ObservableListWrapperWrapper<E> extends ObservableListWrapper<E> {
    public ObservableListWrapperWrapper() {
        super(FXCollections.observableArrayList());
    }
}
