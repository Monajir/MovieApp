module lab.visual.movieapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires java.desktop;

    opens lab.visual.movieapp to javafx.fxml;
    exports lab.visual.movieapp;

    exports lab.visual.movieapp.controller;
    opens lab.visual.movieapp.controller to javafx.fxml;

    exports lab.visual.movieapp.model to com.fasterxml.jackson.databind;
    opens lab.visual.movieapp.model to com.fasterxml.jackson.databind;
}