package cz.zcu.jsmahy.datamining.api;

import java.util.function.BiConsumer;

/**
 * <p>The root node of the data node tree.</p>
 * <p>There can only be <b>ONE</b> root in the whole hierarchy/tree. Methods such as {@link DataNodeImpl#addChild(DataNode)} with
 * {@link DataNodeRootImpl} instance as an argument will throw an {@link IllegalArgumentException}.</p>
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public interface DataNodeRoot<T> extends DataNode<T> {
    /**
     * <p>Iterates over the children of this root.</p>
     * <p>The first argument of the {@link BiConsumer} is the data node, the second argument is the breadth of the node in respect to the
     * parent. In essence, the breadth represents the column. For example:</p>
     * <ul>
     *     <li>(0) Karel IV</li>
     *      <ul>
     *          <li>(1) Jan Lucembursky</li>
     *          <li>(1) Ludvik Bavorsky</li>
     *      </ul>
     *      <li>(0) Jan Lucembursky</li>
     *      <li>(0) ...</li>
     * </ul>
     * <p>In this example the nodes with breadth {@code 1} are children of the parent Karel IV, whereas nodes with breadth {@code 0} are
     * children of root.
     * </p>
     * <p>The first item to consume is the root which contains a null reference to data with depth of -1.</p>
     * <p>The items to consume will be iterated in this order:</p>
     * <ul>
     *     <li>(0) Karel IV</li>
     *     <li>(1) Jan Lucembursky</li>
     *     <li>(1) Ludvik Bavorsky</li>
     *     <li>(0) Jan Lucembursky</li>
     *     <li>(0) ...</li>
     * </ul>
     * <p>Next items always have a depth of {@code >= 0}.</p>
     *
     * @param biConsumer the bi-consumer
     */
    void iterate(BiConsumer<DataNode<T>, Integer> biConsumer);

    /**
     * Root has no data by default.
     *
     * @return nothing
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    T getData() throws UnsupportedOperationException;

    /**
     * @return The display name of the root. If null then there's no display name.
     */
    String getName();

    /**
     * Sets the display name of the root. If null then there's no display name.
     *
     * @param name the display name.
     */
    void setName(String name);
}
