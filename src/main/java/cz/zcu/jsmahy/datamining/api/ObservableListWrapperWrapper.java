package cz.zcu.jsmahy.datamining.api;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.FXCollections;

/**
 * Just a wrapper for {@link ObservableListWrapper} that has an empty constructor. When deserializing via Jackson an empty constructor is required.
 *
 * @param <E> {@inheritDoc}
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
class ObservableListWrapperWrapper<E> extends ObservableListWrapper<E> {
    public ObservableListWrapperWrapper() {
        super(FXCollections.observableArrayList());
    }
}
