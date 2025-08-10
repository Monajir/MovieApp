//package lab.visual.movieapp.service;
//
//import javafx.scene.Scene;
//
//public class ThemeManager {
//    private static ThemeManager instance;
//    private Scene scene;
//    private boolean isDarkMode = true;
//
//    private ThemeManager() {}
//
//    public static ThemeManager getInstance() {
//        if (instance == null) {
//            instance = new ThemeManager();
//        }
//        return instance;
//    }
//
//    public void setScene(Scene scene) {
//        this.scene = scene;
//    }
//
//    public void toggleTheme() {
//        isDarkMode = !isDarkMode;
//        applyTheme();
//    }
//
//    public void applyTheme() {
//        if (scene != null) {
//            scene.getStylesheets().clear();
//            if (isDarkMode) {
//                scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
//            } else {
//                scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
//            }
//        }
//    }
//
//    public boolean isDarkMode() {
//        return isDarkMode;
//    }
//}

package lab.visual.movieapp.service;

import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    private List<Scene> scenes = new ArrayList<>();
    private boolean isDarkMode = true;

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void registerScene(Scene scene) {
        if (!scenes.contains(scene)) {
            scenes.add(scene);
        }
    }

    public void unregisterScene(Scene scene) {
        scenes.remove(scene);
    }

    public void setScene(Scene scene) {
        registerScene(scene);
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    public void applyTheme() {
        String darkThemeUrl = getClass().getResource("/css/dark-theme.css").toExternalForm();
        String lightThemeUrl = getClass().getResource("/css/light-theme.css").toExternalForm();

        // Apply theme to all registered scenes
        for (Scene scene : new ArrayList<>(scenes)) {
            if (scene != null) {
                scene.getStylesheets().clear();
                if (isDarkMode) {
                    scene.getStylesheets().add(darkThemeUrl);
                } else {
                    scene.getStylesheets().add(lightThemeUrl);
                }
            }
        }
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }
}