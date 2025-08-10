package lab.visual.movieapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.visual.movieapp.model.Movie;
import lab.visual.movieapp.model.TMDBResponse;
import javafx.concurrent.Task;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TMDBService {
    private static final String API_KEY = System.getenv("API_KEY"); // Replace with your TMDB API key
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TMDBService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Task<List<Movie>> getPopularMovies(int page) {
        return new Task<List<Movie>>() {
            @Override
            protected List<Movie> call() throws Exception {
                String url = BASE_URL + "/movie/popular?api_key=" + API_KEY + "&page=" + page;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                TMDBResponse tmdbResponse = objectMapper.readValue(response.body(), TMDBResponse.class);
                return tmdbResponse.getResults();
            }
        };
    }

    public Task<List<Movie>> searchMovies(String query, int page) {
        return new Task<List<Movie>>() {
            @Override
            protected List<Movie> call() throws Exception {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encodedQuery + "&page=" + page;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                TMDBResponse tmdbResponse = objectMapper.readValue(response.body(), TMDBResponse.class);
                return tmdbResponse.getResults();
            }
        };
    }

    public Task<Movie> getMovieDetails(int movieId) {
        return new Task<Movie>() {
            @Override
            protected Movie call() throws Exception {
                String url = BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return objectMapper.readValue(response.body(), Movie.class);
            }
        };
    }

    public Task<List<Movie>> getMoviesByGenre(String genre) {
        return new Task<List<Movie>>() {
            @Override
            protected List<Movie> call() throws Exception {
                String url = BASE_URL + "/discover/movie?api_key=" + API_KEY + "&with_genres=" + genre;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                TMDBResponse tmdbResponse = objectMapper.readValue(response.body(), TMDBResponse.class);
                return tmdbResponse.getResults();
            }
        };
    }
}