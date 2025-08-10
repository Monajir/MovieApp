package lab.visual.movieapp.service;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import lab.visual.movieapp.model.Movie;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

// Only storing the movie ids, which again will need to be fetched from tmdb, we might as well store the whole movie
public class WishListService {
    private final MongoCollection<Document> wishlist;
    private final MongoClient mongoClient;

    public WishListService(String connectionString, String dbName) {
        this.mongoClient = MongoClients.create(connectionString); //"mongodb://localhost:27017"
        MongoDatabase database = mongoClient.getDatabase(dbName); //wishlistApp
        this.wishlist = database.getCollection("wishlist");
    }

    public Document movieToDocument(Movie movie) {
        Document doc = new Document("id", movie.getId())
                .append("title", movie.getTitle())
                .append("overview", movie.getOverview())
                .append("posterPath", movie.getPosterPath())
                .append("backdropPath", movie.getBackdropPath())
                .append("releaseDate", movie.getReleaseDate())
                .append("voteAverage", movie.getVoteAverage())
                .append("voteCount", movie.getVoteCount())
                .append("genreIds", movie.getGenreIds())
                .append("adult", movie.isAdult())
                .append("originalLanguage", movie.getOriginalLanguage())
                .append("originalTitle", movie.getOriginalTitle())
                .append("popularity", movie.getPopularity())
                .append("video", movie.isVideo());
        return doc;
    }

    public Movie documentToMovie(Document doc) {
        Movie movie = new Movie();
        movie.setId(doc.getInteger("id"));
        movie.setTitle(doc.getString("title"));
        movie.setOverview(doc.getString("overview"));
        movie.setPosterPath(doc.getString("posterPath"));
        movie.setBackdropPath(doc.getString("backdropPath"));
        movie.setReleaseDate(doc.getString("releaseDate"));
        movie.setVoteAverage(doc.getDouble("voteAverage"));
        movie.setVoteCount(doc.getInteger("voteCount"));
        return movie;
    }

    // Add a whole movie object
    public void addMovie(Movie movie) {
        Document doc = movieToDocument(movie);
        doc.put("_id", movie.getId()); // To avoid duplication
        wishlist.replaceOne(new Document("_id", movie.getId()), doc, new ReplaceOptions().upsert(true));
    }

    // Get all movies
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        for (Document doc : wishlist.find()) {
            movies.add(documentToMovie(doc));
        }
        return movies;
    }

    public boolean isMovieInWishlist(int movieId) {
        Document query = new Document("_id", movieId);
        Document result = wishlist.find(query).first();

        return result != null;
    }

    public void close() { mongoClient.close(); }

    public void removeMovie(int id) {
        wishlist.deleteOne(new org.bson.Document("_id", id));
    }
}
