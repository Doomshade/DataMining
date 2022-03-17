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
    requires org.jsoup;
    requires org.apache.logging.log4j;

    exports cz.zcu.jsmahy.datamining;
    exports cz.zcu.jsmahy.datamining.app.controller;

    opens cz.zcu.jsmahy.datamining;
    opens cz.zcu.jsmahy.datamining.app.controller;
	exports cz.zcu.jsmahy.datamining.config;
	opens cz.zcu.jsmahy.datamining.config;
}