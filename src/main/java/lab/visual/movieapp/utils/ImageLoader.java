package lab.visual.movieapp.utils;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLoader {
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();

    public static Task<Image> loadImageAsync(String url) {
        return new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                if (url == null || url.isEmpty()) {
                    return new Image(getClass().getResourceAsStream("/images/placeholder.png"));
                }

                if (imageCache.containsKey(url)) {
                    return imageCache.get(url);
                }

                Image image = new Image(url, true);
                imageCache.put(url, image);
                return image;
            }
        };
    }
}