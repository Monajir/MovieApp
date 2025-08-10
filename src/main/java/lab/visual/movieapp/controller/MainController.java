package lab.visual.movieapp.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.util.Duration;
import lab.visual.movieapp.model.Movie;
import lab.visual.movieapp.service.TMDBService;
import lab.visual.movieapp.service.ThemeManager;
import lab.visual.movieapp.service.WishListService;
import lab.visual.movieapp.utils.ImageLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    @FXML public Button closeSidebarBtn;
    @FXML private TextField searchField;
    @FXML private FlowPane moviesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private Button themeToggleBtn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label statusLabel;
    @FXML private VBox wishlistSidebar;
    @FXML private Button showSidebarBtn;
    @FXML private ListView<Movie> wishlistListView;
    @FXML private StackPane genreOverlay;
    @FXML private GridPane genreCheckboxGrid;

    private TMDBService tmdbService;
    private WishListService wishListService;
    private ExecutorService executorService;
    private int currentPage = 1;
    private boolean isSearching = false;
    private String currentSearchQuery = "";

    // Genres
    private static final GenreEntry[] GENRES = {
            new GenreEntry(28, "Action"),
            new GenreEntry(12, "Adventure"),
            new GenreEntry(16, "Animation"),
            new GenreEntry(35, "Comedy"),
            new GenreEntry(80, "Crime"),
            new GenreEntry(99, "Documentary"),
            new GenreEntry(18, "Drama"),
            new GenreEntry(10751, "Family"),
            new GenreEntry(14, "Fantasy"),
            new GenreEntry(36, "History"),
            new GenreEntry(27, "Horror"),
            new GenreEntry(10402, "Music"),
            new GenreEntry(9648, "Mystery"),
            new GenreEntry(10749, "Romance"),
            new GenreEntry(878, "Science Fiction"),
            new GenreEntry(10770, "TV Movie"),
            new GenreEntry(53, "Thriller"),
            new GenreEntry(10752, "War"),
            new GenreEntry(37, "Western")
    };
    private static class GenreEntry {
        final int id;
        final String name;
        GenreEntry(int id, String name) { this.id = id; this.name = name; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Added wishListService, Now add the UI and display logic
        wishListService = new WishListService("mongodb://localhost:27017", "wishlistApp");
        tmdbService = new TMDBService();
        executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true); // Make threads daemon so they don't prevent JVM shutdown
            return thread;
        });

        setupUI();
        loadPopularMovies();

        wishlistListView.getStyleClass().add("wishlist-list");
        // WishList Items
        wishlistListView.setCellFactory(listView -> new ListCell<Movie>() {
            private final HBox content = new HBox(10); // spacing between elements
            private final Label title = new Label();
            private final Button removeBtn = new Button("-");
            {

                title.getStyleClass().add("label");
                removeBtn.getStyleClass().add("button");

                content.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(title, removeBtn);
            }

            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);
                if (empty || movie == null) {
                    setGraphic(null);
                } else {
                    title.setText(movie.getTitle());
                    removeBtn.setOnAction(e -> {
                        wishListService.removeMovie(movie.getId());
                        getListView().getItems().remove(movie);
                        // or: refresh the wishlist from DB if other UI is also listening to changes
                    });
                    setGraphic(content);
                }
            }
        });
    }

    private void setupUI() {
        moviesContainer.setHgap(20);
        moviesContainer.setVgap(20);
        moviesContainer.setPadding(new Insets(20));

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.trim().isEmpty()) {
                isSearching = false;
                currentPage = 1;
                loadPopularMovies();
            } else {
                isSearching = true;
                currentSearchQuery = newText.trim();
                currentPage = 1;
                searchMovies();
            }
        });
    }

    @FXML
    private void onThemeToggle() {
        ThemeManager.getInstance().toggleTheme();
        updateThemeButtonText();
        wishlistListView.refresh();
    }

    private void updateThemeButtonText() {
        themeToggleBtn.setText(ThemeManager.getInstance().isDarkMode() ? "☀" : "🌙");
    }

    private void loadPopularMovies() {
        showLoading(true);
        statusLabel.setText("Loading popular movies...");

        Task<List<Movie>> task = tmdbService.getPopularMovies(currentPage);
        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            Platform.runLater(() -> {
                if (currentPage == 1) {
                    moviesContainer.getChildren().clear();
                }
                displayMovies(movies);
                showLoading(false);
                statusLabel.setText("Loaded " + movies.size() + " movies");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showLoading(false);
                statusLabel.setText("Failed to load movies");
                showAlert("Error", "Failed to load movies: " + task.getException().getMessage());
            });
        });

        executorService.submit(task);
    }

    private void searchMovies() {
        showLoading(true);
        statusLabel.setText("Searching movies...");

        Task<List<Movie>> task = tmdbService.searchMovies(currentSearchQuery, currentPage);
        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            Platform.runLater(() -> {
                if (currentPage == 1) {
                    moviesContainer.getChildren().clear();
                }
                displayMovies(movies);
                showLoading(false);
                statusLabel.setText("Found " + movies.size() + " movies");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showLoading(false);
                statusLabel.setText("Search failed");
                showAlert("Error", "Search failed: " + task.getException().getMessage());
            });
        });

        executorService.submit(task);
    }

    private void displayMovies(List<Movie> movies) {
        for (Movie movie : movies) {
            VBox movieCard = createMovieCard(movie);
            moviesContainer.getChildren().add(movieCard);
        }
    }

    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setPrefWidth(200);
        card.setPrefHeight(350);

        // Poster ImageView
        ImageView posterView = new ImageView();
        posterView.setFitWidth(180);
        posterView.setFitHeight(270);
        posterView.setPreserveRatio(true);
        posterView.getStyleClass().add("movie-poster");

        // Load image asynchronously
        Task<javafx.scene.image.Image> imageTask = ImageLoader.loadImageAsync(movie.getFullPosterUrl());
        imageTask.setOnSucceeded(e -> posterView.setImage(imageTask.getValue()));
        executorService.submit(imageTask);

        // Title Label
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-title");
        titleLabel.setWrapText(true);
        titleLabel.setPrefWidth(180);

        // Rating Label
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", movie.getVoteAverage()));
        ratingLabel.getStyleClass().add("movie-rating");

        card.getChildren().addAll(posterView, titleLabel, ratingLabel);

        // Click handler
        card.setOnMouseClicked(e -> openMovieDetail(movie));

        return card;
    }

    private void openMovieDetail(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab/visual/movieapp/movieDetail.fxml"));
            Parent root = loader.load();

            MovieDetailController controller = loader.getController();
            controller.setMovie(movie);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setTitle(movie.getTitle());

            Scene scene = new Scene(root, 800, 800);

            // Register the new scene with ThemeManager
            ThemeManager.getInstance().registerScene(scene);
            ThemeManager.getInstance().applyTheme();

            stage.setScene(scene);

            // Clean up when window is closed
            stage.setOnCloseRequest(event -> {
                ThemeManager.getInstance().unregisterScene(scene);
                stage.close();
            });

            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to open movie details: " + e.getMessage());
        }
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void loadMoreMovies() {
        currentPage++;
        if (isSearching) {
            searchMovies();
        } else {
            loadPopularMovies();
        }
    }

    @FXML
    private void showSidebar() {
        wishlistSidebar.setVisible(true);
        wishlistSidebar.setManaged(true);
        showSidebarBtn.setVisible(false);

        wishlistSidebar.setTranslateX(300); // move offscreen
        wishlistSidebar.setOpacity(0);
        wishlistSidebar.toFront();

        // Animate slide in (300px width)
        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(wishlistSidebar.translateXProperty(), 300),
                        new KeyValue(wishlistSidebar.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(wishlistSidebar.translateXProperty(), 0),
                        new KeyValue(wishlistSidebar.opacityProperty(), 1))
        );
        slideIn.play();

        refreshWishlist();


    }

    @FXML
    private void hideSidebar() {
        Timeline slideOut = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(wishlistSidebar.translateXProperty(), 0),
                        new KeyValue(wishlistSidebar.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(wishlistSidebar.translateXProperty(), 300),
                        new KeyValue(wishlistSidebar.opacityProperty(), 0))
        );
        slideOut.setOnFinished(ev -> {
            wishlistSidebar.setVisible(false);
            wishlistSidebar.setManaged(false);
            showSidebarBtn.setVisible(true);
        });
        slideOut.play();
    }

    @FXML
    public void refreshWishlist() {
        List<Movie> wishlistMovies = wishListService.getAllMovies();
        wishlistListView.getItems().setAll(wishlistMovies);
    }

    private final Map<Integer, CheckBox> genreCheckboxes = new HashMap<>();

    private void setupGenreCheckboxes() {
        genreCheckboxGrid.getChildren().clear();
        int cols = 2; // 2 columns for neatness
        for (int i = 0; i < GENRES.length; i++) {
            GenreEntry genre = GENRES[i];
            CheckBox cb = new CheckBox(genre.name);
            cb.getStyleClass().add("genre-checkbox");
            genreCheckboxes.put(genre.id, cb);
            genreCheckboxGrid.add(cb, i % cols, i / cols);
        }
    }

    @FXML private void showGenreOverlay() {
        genreOverlay.setVisible(true);
        genreOverlay.setManaged(true);
        genreOverlay.toFront();
        setupGenreCheckboxes();
    }
    @FXML private void hideGenreOverlay() {
        genreOverlay.setVisible(false);
        genreOverlay.setManaged(false);
    }

    @FXML private void applyGenreFilter() {
        // Collect all selected genre ids
        List<Integer> selectedGenres = genreCheckboxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();

        hideGenreOverlay();

        if (!selectedGenres.isEmpty()) {
            String genreIds = selectedGenres.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")); // 28,35,18 etc

            fetchMoviesByGenre(genreIds);
        } else {
            // Optionally, show warning or reset to default movie list
        }
    }

    private void fetchMoviesByGenre(String genreIds) {
        showLoading(true);
        statusLabel.setText("Loading movies by genre...");

        Task<List<Movie>> task = tmdbService.getMoviesByGenre(genreIds);
        task.setOnSucceeded(e -> {
            List<Movie> movies = task.getValue();
            Platform.runLater(() -> {
                moviesContainer.getChildren().clear();

                displayMovies(movies);
                showLoading(false);
                statusLabel.setText("Loaded " + movies.size() + " movies");
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showLoading(false);
                statusLabel.setText("Failed to load movies");
                showAlert("Error", "Failed to load movies: " + task.getException().getMessage());
            });
        });

        executorService.submit(task);
    }
}