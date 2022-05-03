module de.fhzwickau.graphmlbuilder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens de.fhzwickau.graphbuilder to javafx.fxml;
    exports de.fhzwickau.graphbuilder;
    exports de.fhzwickau.graphbuilder.controller;
    opens de.fhzwickau.graphbuilder.controller to javafx.fxml;
}