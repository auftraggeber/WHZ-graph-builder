package de.fhzwickau.roomfinder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GraphBuilderApplication extends Application {

    private static final String TITLE = "GraphBuilder";
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GraphBuilderApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600,450);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();

        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void start() {
        launch();
    }
}