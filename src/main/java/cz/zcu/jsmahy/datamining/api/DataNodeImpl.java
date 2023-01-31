package cz.zcu.jsmahy.datamining.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.*;


@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
class DataNodeImpl<T> implements DataNode<T> {
    private static long ID_SEQ = 0;

    @EqualsAndHashCode.Exclude
    private final T data;
    private final DataNode<T> parent;
    private final long id;
    private final ObservableList<DataNode<T>> children = FXCollections.observableArrayList();
    private final Map<String, Object> metadata = new HashMap<>();
    private String name;
    private String uri;
    private Date startDate;
    private Date endDate;

    {
        this.id = ID_SEQ++;
    }

    protected DataNodeImpl() {
        this.data = null;
        this.parent = null;
    }

    DataNodeImpl(final @NonNull T data, final DataNode<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    /**
     * Adds a child to this node.
     *
     * @param child the child to add to this node
     *
     * @throws IllegalArgumentException if the child is an instance of {@link DataNodeRoot}
     * @throws NullPointerException     if the child is {@code null}
     */
    public void addChild(@NonNull DataNode<T> child) throws IllegalArgumentException, NullPointerException {
        assert !(child instanceof DataNodeRoot<T>);
        this.children.add(child);
    }

    @Override
    public ObservableList<DataNode<T>> getChildren() {
        return FXCollections.unmodifiableObservableList(children);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Optional<V> getMetadataValue(final String key) throws ClassCastException {
        return (Optional<V>) Optional.ofNullable(metadata.get(key));
    }

    @Override
    public void addMetadata(final String key, final Object value) {
        this.metadata.put(key, value);
    }

    @Override
    public Optional<DataNodeRoot<T>> findRoot() {
        DataNode<T> prev = parent;
        while (prev != null && prev.getParent() != null) {
            prev = prev.getParent();
        }
        if (prev != null) {
            assert prev instanceof DataNodeRoot<T>; // the upmost parent should always be root
            return Optional.of((DataNodeRoot<T>) prev);
        }
        return Optional.empty();
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public Iterator<DataNode<T>> iterator() {
        return children.iterator();
    }
}
