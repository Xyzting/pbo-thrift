package com.example;

import com.example.service.AudioManager;
import com.example.util.AlertHelper;
import com.example.util.DatabaseConnection;
import com.example.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseInitializer.init();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error",
                "Tidak dapat terhubung ke MySQL:\n" + e.getMessage()
                + "\n\nPastikan MySQL Server sudah berjalan dan konfigurasi di DatabaseConnection.java sudah benar.");
            Platform.exit();
            return;
        }

        AudioManager.getInstance().init();
        AudioManager.getInstance().playBGM();

        scene = new Scene(loadFXML("login"), 960, 620);
        stage.setTitle("Thrift UMKM Store");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        AudioManager.getInstance().dispose();
        DatabaseConnection.close();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/view/" + fxml + ".fxml"));
        return loader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
