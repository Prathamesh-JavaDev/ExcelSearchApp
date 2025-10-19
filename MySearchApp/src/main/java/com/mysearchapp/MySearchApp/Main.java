package com.mysearchapp.MySearchApp;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
        	//updated code line for ControllerLoader addition
        	Parent root = com.mysearchapp.MySearchApp.loader.ControllerLoader.load();

            primaryStage.setTitle("Excel Search Application");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
