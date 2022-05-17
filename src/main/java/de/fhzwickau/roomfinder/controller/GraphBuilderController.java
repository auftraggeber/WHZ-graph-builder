package de.fhzwickau.roomfinder.controller;

import de.fhzwickau.roomfinder.GraphBuilderApplication;
import de.fhzwickau.roomfinder.model.graph.Graph;
import de.fhzwickau.roomfinder.model.graph.edge.Edge;
import de.fhzwickau.roomfinder.model.graph.node.LazyNode;
import de.fhzwickau.roomfinder.model.graph.node.Node;
import de.fhzwickau.roomfinder.model.metadata.Metadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

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
            NODE_ALREADY_EXISTS = "Der Knoten existiert bereits.",
            WRONG_WEIGHT_INPUT = "Wählen Sie für das Gewicht eine natürliche Zahl.",
            WRONG_NODE_ID_INPUT = "Geben Sie gültige IDs für die Knoten an.",
            EDGE_ALREADY_ADDED = "Diese Kante existiert bereits.",
            UNKNOWN_ERROR = "Es ist ein unerwarteter Fehler aufgetreten.",
            EDGE_ADDED = "Die Kante konnte hinzugefügt werden.";

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
    private TableView<Node> linkedNodesTableView;
    @FXML
    private TableColumn<Node, String> linkedNodesColumn;

    @FXML
    private TextField edgeNode1TextField;
    @FXML
    private TextField edgeNode2TextField;
    @FXML
    private TextField edgeWeightTextField;

    @FXML
    private void initialize() {
        graph = new Graph();

        Callback<TableColumn.CellDataFeatures<Node, String>, ObservableValue<String>> callback = v -> {
            String lazyNode = v.getValue() instanceof LazyNode ? " (LazyNode)" : "";

            return new SimpleStringProperty(v.getValue().getId() + lazyNode);
        };

        nodeNameColumn.setCellValueFactory(callback);
        linkedNodesColumn.setCellValueFactory(callback);

        nodeTableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Node>) change -> {
            linkedNodesTableView.getItems().clear();

            Node selected = nodeTableView.getSelectionModel().getSelectedItem();

            if (selected != null) {
                List<Node> links = new ArrayList<>();

                for (Edge e : selected.getEdges()) {
                    links.add(e.getOther(selected));
                }

                linkedNodesTableView.setItems(FXCollections.observableList(links));
            }

        });
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

    @FXML
    private void saveEdge() {

        try {
            Node node1 = getNodeFromString(edgeNode1TextField.getText());
            Node node2 = getNodeFromString(edgeNode2TextField.getText());

            if (node1 == null) {
                node1 = new LazyNode(edgeNode1TextField.getText());
                graph.add(node1);
                reloadExistingNodes();
            }
            if (node2 == null) {
                node2 = new LazyNode(edgeNode2TextField.getText());
                graph.add(node2);
                reloadExistingNodes();
            }

            int weight = Integer.parseInt(edgeWeightTextField.getText());

            if (weight <= 0) {
                new Alert(Alert.AlertType.ERROR, WRONG_WEIGHT_INPUT).show();
            }

            try {
                Edge e = new Edge(node1, node2, weight);

                if (node1.addEdge(e) | node2.addEdge(e)) {

                    // eine ODER-Verknüpfung, aber falls das erste TRUE ist, wird das 2. trotzdem ausgeführt!
                    new Alert(Alert.AlertType.CONFIRMATION, EDGE_ADDED).show();
                }
                else {
                    new Alert(Alert.AlertType.ERROR, UNKNOWN_ERROR).show();
                }
            }
            catch (RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR, WRONG_NODE_ID_INPUT).show();
            }
        } catch (RuntimeException ex) {
            new Alert(Alert.AlertType.ERROR, WRONG_WEIGHT_INPUT).show();
        }

    }

    /**
     * Sucht eine Node, die zu einer Node gehört.
     * @param id Die ID, die zu einer Node gehört.
     * @return Gibt die Node oder null zurück.
     */
    private Node getNodeFromString(String id) {
        if (id == null)
            return null;

        if (graph.containsKey(id))
            return (Node) graph.get(id);

        return null;
    }

    /**
     * Verucht einen Knoten anhand des Inputs in die {@link #searchBar} auslesen.
     * @return Der gefundene Knoten oder null.
     */
    private Node getNodeFromSearchBar() {
        String searchForID = searchBar.getText();

        return getNodeFromString(searchForID);
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

        //autoCompletionBinding = TextFields.bindAutoCompletion(searchBar, graph.keySet());
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