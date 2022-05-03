package de.fhzwickau.graphbuilder.controller;

import de.fhzwickau.graphbuilder.model.graph.Graph;
import de.fhzwickau.graphbuilder.model.graph.node.Node;
import de.fhzwickau.graphbuilder.model.metadata.Metadata;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Die Klasse, die die Eingaben des UIs in das Modell überträgt.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class GraphBuilderController {

    private static final String NO_NODE_WARNING = "Es wurde kein Knoten ausgewählt.",
    NODE_ADDED = "Knoten wurde erfolgreich hinzugefügt.", NULL_MESSAGE_END = " ist null. Bitte beheben!", WRONG_DATATYPE = "%1% konnte nicht in \"%2%\" umgeandelt werden.";

    private Node modify;
    private Graph graph;
    private Map<Field, TextField> textFields = new HashMap<>();

    @FXML
    private GridPane pane;

    @FXML
    private void initialize() {
        graph = new Graph();
    }

    @FXML
    private void save() {
        if (modify != null) {
            for (Field field : modify.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Metadata.class)) {
                    String value = getUserInputFor(field);

                    if (value == null && !field.getAnnotation(Metadata.class).nullable()) {
                        new Alert(Alert.AlertType.ERROR, field.getName() + NULL_MESSAGE_END).show();

                        return;
                    }

                    try {
                        Object v = castFor(field, value);

                        try {
                            field.setAccessible(true);
                            field.set(modify, v);
                            field.setAccessible(false);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    catch (RuntimeException ex) {
                        new Alert(Alert.AlertType.ERROR, WRONG_DATATYPE.replace("%1%", field.getName()).replace("%2%", field.getType().getSimpleName())).show();

                        return;
                    }

                }
            }

            graph.add(modify);

            if (graph.contains(modify)) {
                new Alert(Alert.AlertType.INFORMATION, NODE_ADDED).show();
            }
        }
        else {
            new Alert(Alert.AlertType.WARNING, NO_NODE_WARNING).show();
        }
    }

    @FXML
    private void createNewNode() {

        modify = new Node();

        loadFields(modify.getClass());
    }

    /**
     * Ermittelt die Nutzereingabe für ein bestimmtes Feld.
     * @param field Das Feld, für welches die Nutzereingabe abgefragt wurde.
     * @return Die Nutzereingabe als String. "" entspricht dabei null.
     */
    private String getUserInputFor(Field field) {
        TextField textField = textFields.get(field);
        String value = textField != null ? textField.getText() : null;

        if ("".equals(value)) {
            value = null;
        }

        return value;
    }

    /**
     * Baut die Beschreibung für das UI zusammen.
     * @param field Das Feld, für das die Beschreibung gebaut werden soll.
     * @return Die Beschreibung.
     */
    private String getDescription(Field field) {
        if (field.isAnnotationPresent(Metadata.class)) {
            Metadata metadata = field.getAnnotation(Metadata.class);

            String d = metadata.description() != null ? metadata.description() : "";

            return "(" + field.getType().getSimpleName() + ") " + field.getName() + "=\n" + d + "\n";
        }

        return "";
    }

    /**
     * Lädt alle Felder einer bestimmten Klasse, die die {@link Metadata} Annotation haben, in das UI.
     * @param forClass Die Klasse, für die die Felder angezeigt werden sollen.
     */
    private void loadFields(Class<?> forClass) {
        pane.getChildren().removeAll(pane.getChildren());
        textFields.clear();
        int i = 0;

        for (Field field : forClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Metadata.class)) {
                field.setAccessible(true);
                Object v = null;
                try {
                    v = field.get(modify);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);

                TextField textField = new TextField();
                textField.setPromptText(field.getName());
                if (v != null)
                    textField.setText(v.toString());

                textFields.put(field, textField);

                pane.addRow(i, new Label(getDescription(field)), textField);
                i++;
            }
        }

        pane.setGridLinesVisible(true);
    }

    /**
     * Wandelt eine Nutzereingabe in einen bestimmten Typen um.
     * Das Casting muss hier per Hand erfolgen. Für alle möglichen Feldtypen muss die Castingmethode hier existieren.
     * @param field Das Feld, für dessen Typen die Eingabe angepasst werden soll.
     * @param value Die Nutzereingabe.
     * @return Der Wert, welcher für das angebene Feld gecastet ist.
     */
    private Object castFor(Field field, String value) {
        if (field.getType().equals(String.class)) {
            return value;
        }
        else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            return Integer.parseInt(value);
        }
        else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }

        return null;
    }

}