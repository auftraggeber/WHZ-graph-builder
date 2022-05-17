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

    opens de.fhzwickau.roomfinder to javafx.fxml;
    exports de.fhzwickau.roomfinder;
    exports de.fhzwickau.roomfinder.controller;
    opens de.fhzwickau.roomfinder.controller to javafx.fxml;
}