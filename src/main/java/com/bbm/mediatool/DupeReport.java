package com.bbm.mediatool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bbm.mediatool.entity.Movie;

public class DupeReport {

	public void report(File kodiLibFile) {

		Map<String,Movie> movies = new HashMap<>();
		List<Movie> dupeMovies = new ArrayList<>();

		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + kodiLibFile.getAbsolutePath());
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select c00, c07, c22, c09 from movie");
			while (rs.next()) {
				String title = rs.getString(1);
				String year = rs.getString(2);
				String fileStr = rs.getString(3);
				String imdbId = rs.getString(4);

				Movie movie = new Movie();
				movie.setTitle(title);
				movie.setYear(Integer.parseInt(year));
				movie.setFiles(fileStr);
				movie.setImdbId(imdbId);
				if (imdbId == null || imdbId.length() == 0) {
					System.out.println("<<< ignoring title not found in IMDB " + title + " (" + year + ") >>>");
					continue;
				}
				if (movies.containsKey(imdbId)) {
					dupeMovies.add(movies.get(imdbId));
					dupeMovies.add(movie);
				} else {
					movies.put(imdbId,movie);
				}
//				System.out.println(movie);
				
			}
			
			for (Movie m : dupeMovies) {
				System.out.println(m);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}

	}

}
