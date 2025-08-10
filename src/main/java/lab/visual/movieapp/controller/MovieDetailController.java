package lab.visual.movieapp.controller;

import javafx.scene.control.Button;
import lab.visual.movieapp.model.Movie;
import lab.visual.movieapp.service.WishListService;
import lab.visual.movieapp.utils.ImageLoader;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailController implements Initializable {

    @FXML private ImageView backdropImage;
    @FXML private ImageView posterImage;
    @FXML private Label titleLabel;
    @FXML private Label overviewLabel;
    @FXML private Label releaseDateLabel;
    @FXML private Label ratingLabel;
    @FXML private Label voteCountLabel;
    @FXML private VBox detailContainer;
    @FXML private Button addToWishlistButton;

    private ExecutorService executorService;
    private Movie movie;
    private WishListService wishListService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executorService = Executors.newCachedThreadPool();
        wishListService = new WishListService("mongodb://localhost:27017", "wishlistApp");
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        displayMovieDetails();
    }

    @FXML
    private void displayMovieDetails() {
        if (movie == null) return;

        titleLabel.setText(movie.getTitle());
        overviewLabel.setText(movie.getOverview());
        releaseDateLabel.setText("Release Date: " + movie.getReleaseDate());
        ratingLabel.setText("Rating: ⭐ " + String.format("%.1f", movie.getVoteAverage()));
        voteCountLabel.setText("Votes: " + movie.getVoteCount());

        if (wishListService.isMovieInWishlist(movie.getId())) {
            addToWishlistButton.setText("✓");
            addToWishlistButton.setDisable(true); // Optionally disable if already added
        } else {
            addToWishlistButton.setText("+");
            addToWishlistButton.setDisable(false);
        }

        // Load backdrop image
        if (movie.getFullBackdropUrl() != null) {
            Task<javafx.scene.image.Image> backdropTask = ImageLoader.loadImageAsync(movie.getFullBackdropUrl());
            backdropTask.setOnSucceeded(e -> backdropImage.setImage(backdropTask.getValue()));
            executorService.submit(backdropTask);
        }

        // Load poster image
        if (movie.getFullPosterUrl() != null) {
            Task<javafx.scene.image.Image> posterTask = ImageLoader.loadImageAsync(movie.getFullPosterUrl());
            posterTask.setOnSucceeded(e -> posterImage.setImage(posterTask.getValue()));
            executorService.submit(posterTask);
        }
    }

    @FXML
    public void handleAddToWishlist(javafx.event.ActionEvent actionEvent) {
        // Add the current movie to the wishlist
        wishListService.addMovie(movie);

        // Set button to show a tick and disable it
        addToWishlistButton.setText("✓");
        addToWishlistButton.setDisable(true); // Prevent adding again

        // Optionally, display a toast or message for confirmation
    }
}