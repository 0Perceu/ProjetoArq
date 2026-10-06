// Carrega a janela inicial

package com.simumic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override //Sobrescreve a função start da biblioteca
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent root = loader.load();
        Rectangle2D area = Screen.getPrimary().getVisualBounds();
        scene = new Scene(root, Math.min(1450, area.getWidth() * 0.95),
                Math.min(1000, area.getHeight() * 0.95));
        stage.setTitle("ProjetoArq — Simulador MIC-1");
        stage.setScene(scene);
        PrimaryController controller = loader.getController();
        stage.setOnCloseRequest(event -> controller.encerrar());
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml")); // carrega xml
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
