package cz.zcu.jsmahy.datamining.api;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class ObservableListWrapperWrapper<E> extends ObservableListWrapper<E> {
    public ObservableListWrapperWrapper() {
        this(new ArrayList<>());
    }

    public ObservableListWrapperWrapper(final List<E> list) {
        super(list);
    }

    public ObservableListWrapperWrapper(final List<E> list, final Callback<E, Observable[]> extractor) {
        super(list, extractor);
    }
}
