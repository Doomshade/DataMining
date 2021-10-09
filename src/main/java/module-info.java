/**
 * @author Jakub Šmrha
 */module DataMining {
    requires javafx.base;
    requires javafx.media;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires com.jfoenix;
    requires commons.cli;
    requires java.logging;
    requires org.apache.jena.core;

    exports git.doomshade.datamining;
    exports git.doomshade.datamining.app.controller;

    opens git.doomshade.datamining;
    opens git.doomshade.datamining.app.controller;
}