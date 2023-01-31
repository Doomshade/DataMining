package cz.zcu.jsmahy.datamining.api;

import javafx.scene.layout.Pane;

/**
 * Implementations of this interface are responsible for visualizing the time series. (TODO: rename)
 *
 * @author Jakub Šmrha
 * @since 1.0
 */
public interface Visualizer {
    <T> void render(Pane pane, DataNode root);
}
