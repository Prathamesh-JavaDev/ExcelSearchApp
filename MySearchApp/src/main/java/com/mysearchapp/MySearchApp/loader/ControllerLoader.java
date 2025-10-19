package com.mysearchapp.MySearchApp.loader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

/**
 * Dynamically loads the latest working Controller + FXML version.
 * Falls back gracefully if one of them is missing.
 */
public final class ControllerLoader {

    private static final String BASE_FXML = "/com/mysearchapp/MySearchApp/main_view.fxml";
    private static final String FXML_DIR = "/com/mysearchapp/MySearchApp/";
    private static final String CONTROLLER_PACKAGE = "com.mysearchapp.MySearchApp.controller";
    private static final String BASE_CONTROLLER = CONTROLLER_PACKAGE + ".MainController";

    private ControllerLoader() {}

    public static Parent load() throws IOException {
        int latestValidVersion = findLatestValidVersion();

        // Determine FXML to load
        String fxmlToLoad = (latestValidVersion == 0)
                ? BASE_FXML
                : FXML_DIR + "main_view_v" + latestValidVersion + ".fxml";

        URL fxmlUrl = ControllerLoader.class.getResource(fxmlToLoad);
        if (fxmlUrl == null)
            throw new IOException("FXML not found: " + fxmlToLoad);

        FXMLLoader loader = new FXMLLoader(fxmlUrl);

        // Get corresponding controller (fallback-safe)
        Object controller = findControllerForVersion(latestValidVersion);
        if (controller == null) controller = createBaseController();

        loader.setController(controller);

        System.out.println("✅ Using controller: " + controller.getClass().getSimpleName()
                + " with FXML: " + fxmlToLoad);

        return loader.load();
    }

    /**
     * Finds the latest version where both the FXML and Controller exist.
     * If mismatch occurs, uses the last version that was valid.
     */
    private static int findLatestValidVersion() {
        int version = 1;
        int lastValid = 0;

        while (true) {
            String fxmlFile = FXML_DIR + "main_view_v" + version + ".fxml";
            String controllerClass = CONTROLLER_PACKAGE + ".MainController_V" + version;

            boolean fxmlExists = ControllerLoader.class.getResource(fxmlFile) != null;
            boolean controllerExists;
            try {
                Class.forName(controllerClass);
                controllerExists = true;
            } catch (ClassNotFoundException e) {
                controllerExists = false;
            }

            if (fxmlExists && controllerExists) {
                lastValid = version; // both exist → valid combo
            } else if (fxmlExists || controllerExists) {
                System.out.println("⚠️ Partial version found (FXML or Controller missing) for V" + version);
            } else {
                break; // no more versions ahead
            }
            version++;
        }
        return lastValid;
    }

    private static Object findControllerForVersion(int version) {
        if (version <= 0) return null;
        String className = CONTROLLER_PACKAGE + ".MainController_V" + version;
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("⚠️ Controller V" + version + " failed to load. Falling back.");
            return null;
        }
    }

    private static Object createBaseController() {
        try {
            return Class.forName(BASE_CONTROLLER).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.out.println("❌ Failed to load base controller: " + BASE_CONTROLLER);
            return null;
        }
    }
}
