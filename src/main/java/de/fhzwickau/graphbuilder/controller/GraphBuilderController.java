package de.fhzwickau.graphbuilder.controller;

import de.fhzwickau.graphbuilder.GraphBuilderApplication;
import de.fhzwickau.graphbuilder.model.graph.Graph;
import de.fhzwickau.graphbuilder.model.graph.node.LazyNode;
import de.fhzwickau.graphbuilder.model.graph.node.Node;
import de.fhzwickau.graphbuilder.model.metadata.Metadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Klasse, die die Eingaben des UIs in das Modell überträgt.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class GraphBuilderController {

    private static final String NO_NODE_WARNING = "Es wurde kein Knoten ausgewählt.",
            NODE_ADDED = "Knoten wurde erfolgreich hinzugefügt.",
            NULL_MESSAGE_END = " ist null. Bitte beheben!",
            WRONG_DATATYPE = "%1% konnte nicht in \"%2%\" umgeandelt werden.",
            NODE_NOT_FOUND = "Es konnte kein Knoten mit dieser ID gefunden werden.",
            SAVED = "Die Daten konnten gespeichert werden.",
            SAVE_ERROR = "Es gab einen unerwarteten Fehler beim Speichern.",
            LOADED = "Der Graph konnte geladen werden.",
            NODE_REMOVED = "Der Knoten konnte entfernt werden.",
            NODE_ALREADY_EXISTS = "Der Knoten existiert bereits.";

    private static final String CHOOSER_EXPORT_TITLE = "Exportieren",
    CHOOSER_IMPORT_TITLE = "Importieren";

    private static final FileChooser.ExtensionFilter FILE_FILTER = new FileChooser.ExtensionFilter("Graphen (*.grser)", "*.grser");

    private Node modify;
    private Graph graph;
    private Map<Field, TextField> textFields = new HashMap<>();
    private AutoCompletionBinding<String> autoCompletionBinding;

    @FXML
    private GridPane pane;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Node> nodeTableView;
    @FXML
    private TableColumn<Node, String> nodeNameColumn;

    @FXML
    private void initialize() {
        graph = new Graph();

        nodeNameColumn.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getId()));
    }

    /**
     * Speichert eine {@link Node}, die aktuell im Bearbetungsfenster ist ({@link #modify}).
     */
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

            if (graph.contains(modify))
                graph.remove(modify);

            graph.add(modify);

            if (graph.contains(modify)) {
                new Alert(Alert.AlertType.INFORMATION, NODE_ADDED).show();
            }

            reloadExistingNodes();
            modify = null;
            loadFields(null);
        }
        else {
            new Alert(Alert.AlertType.WARNING, NO_NODE_WARNING).show();
        }

    }

    /**
     * Erstellt eine neue {@link Node} un lädt sie ins Bearbetungsfenster.
     */
    @FXML
    private void createNewNode() {
        modify = new Node();

        loadFields(modify);
    }

    /**
     * Diese Methode lädt eine schon existierende {@link Node} in das Bearbetungsfenster.
     * Dabei wird die eingebene ID aus {@link #searchBar} ausgelesen und das zugehörige Element geladen.
     */
    @FXML
    private void loadNode() {
        Node node = getNodeFromSearchBar();

        if (node != null) {
            modify = node;

            loadFields(node);
        }
        else {
            new Alert(Alert.AlertType.ERROR, NODE_NOT_FOUND).show();
        }

        searchBar.setText("");
    }

    @FXML
    private void removeNode() {
        Node node = getNodeFromSearchBar();

        if (node != null) {
            graph.remove(node);

            if (!graph.contains(node)) {
                new Alert(Alert.AlertType.CONFIRMATION, NODE_REMOVED).show();
            }
        }
        else new Alert(Alert.AlertType.ERROR, NODE_NOT_FOUND).show();

        searchBar.setText("");

        reloadExistingNodes();
    }

    /**
     * Diese Methode öffnet einen {@link FileChooser} zum Abspeichern des angelegten Graphen.
     */
    @FXML
    private void exportGraph() {
        File file = buildFileChooser(CHOOSER_EXPORT_TITLE).showSaveDialog(GraphBuilderApplication.getPrimaryStage());

        if (file != null) {
            try {
                FileOutputStream fileOut = new FileOutputStream(file);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);

                objectOut.writeObject(graph);

                objectOut.flush();
                objectOut.close();

                new Alert(Alert.AlertType.CONFIRMATION, SAVED).show();
            } catch (IOException | SecurityException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, SAVE_ERROR);
                alert.setContentText(ex.getLocalizedMessage());
                alert.show();
            }
        }
    }

    /**
     * Diese Methode öffnet einen {@link FileChooser} und liest einen persistierten Graphen ein.
     */
    @FXML
    private void importGraph() {
        File file = buildFileChooser(CHOOSER_IMPORT_TITLE).showOpenDialog(GraphBuilderApplication.getPrimaryStage());

        if (file != null) {
            try {
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);

                Graph temp = (Graph) objectIn.readObject();
                objectIn.close();

                graph.addAll(temp);

                new Alert(Alert.AlertType.CONFIRMATION, LOADED).show();

                reloadExistingNodes();
            } catch (IOException | SecurityException | ClassNotFoundException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, SAVE_ERROR);
                alert.setContentText(ex.getLocalizedMessage());
                alert.show();
            }
        }
    }

    /**
     * Verucht einen Knoten anhand des Inputs in die {@link #searchBar} auslesen.
     * @return Der gefundene Knoten oder null.
     */
    private Node getNodeFromSearchBar() {
        String searchForID = searchBar.getText();

        if (graph.containsKey(searchForID))
            return graph.get(searchForID);

        return null;
    }

    /**
     * Baut einen {@link FileChooser}.
     * Es sind nur Dateien, die auf .grser enden, zulässig.
     * @param title Der Titel des Choosers.
     * @return Der zusammengebaute Chooser.
     */
    private FileChooser buildFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);

        chooser.getExtensionFilters().add(FILE_FILTER);

        return chooser;
    }

    /**
     * Lädt alle {@link Node}s in das UI, die zum Graphen hinzugefügt wurden.
     * Die Nodes müssen zu {@link #graph} gehören.
     */
    private void reloadExistingNodes() {
        List<Node> values = new ArrayList<>();

        for (Node node : graph.values()) {
            values.add(node);
            System.out.println(node.getId());
        }

        nodeTableView.getItems().clear();
        nodeTableView.setItems(FXCollections.observableList(values));

        if (autoCompletionBinding != null)
            autoCompletionBinding.dispose();

        autoCompletionBinding = TextFields.bindAutoCompletion(searchBar, graph.keySet());
    }

    /**
     * Ermittelt die Nutzereingabe für ein bestimmtes Attribut.
     * Es wird anhand des aktuell zu bearbeitenden Knotens {@link #modify} und des hier angegeben Atrributfeldes
     * der dazugehörige Nutzerinput in das dazugehörende {@link TextField} (siehe {@link #textFields}) ermittelt
     * @param field Das Feld, für welches die Nutzereingabe abgefragt wurde.
     * @return Die Nutzereingabe als String. "" wird dabei in null umgewandelt.
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
     * Baut die Beschreibung eines Attributes für das UI zusammen.
     * Die Beschreibung steht in {@link Metadata#description()}.
     * @param field Das Feld, für das die Beschreibung gebaut werden soll. Es muss mit {@link Metadata} gekennzeichnet sein.
     * @return Die Beschreibung für das Attribut.
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
     * Sie werden in die {@link #pane} geladen. Dadurch kann der Nutzer die Attribute des Objektes setzen.
     * @param node Das Objekt, für das die Felder generiert werden sollen.
     */
    private void loadFields(Node node) {
        pane.getChildren().removeAll(pane.getChildren());
        textFields.clear();
        int i = 0;

        if (node == null || node instanceof LazyNode)
            return; // LazyNodes solen nur die ID der richtigen Nodes halten

        for (Field field : node.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Metadata.class)) {
                field.setAccessible(true);
                Object v = null;
                try {
                    v = field.get(node);
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