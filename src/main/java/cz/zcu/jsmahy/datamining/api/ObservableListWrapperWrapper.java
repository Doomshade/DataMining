package cz.zcu.jsmahy.datamining.api;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.FXCollections;

class ObservableListWrapperWrapper<E> extends ObservableListWrapper<E> {
    public ObservableListWrapperWrapper() {
        super(FXCollections.observableArrayList());
    }
}
