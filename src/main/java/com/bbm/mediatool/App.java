package com.bbm.mediatool;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.bbm.mediatool.entity.Movie;

public class App {

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption(new Option("h", "help", false, "print this message"));

		options.addOption(
				new Option("r", "report", false, "Report what movies will be copied. Copies nothing - just report."));
		options.addOption(new Option("c", "copy", false, "Attempt to copy the new movies."));
		options.addOption(new Option("v", "dvd", false,
				"Include movies that appear to be a folder of DVD format. These can be huge."));
		options.addOption(new Option("l", "library", true, "File location of Kodi user data for the existing library"));
		options.addOption(
				new Option("s", "source", true, "File location of Kodi user data for the movies to be copied"));
		options.addOption(new Option("d", "destination", true, "Directory location where movies will be copied to"));
		options.addOption(new Option("k", "skipNonIMDB", false, "skip movies that are not found by IMDB.com"));
		options.addOption(
				new Option("b", "copyBetter", false, "Copy duplicate movies that are probably better quality"));

		String header = "Use Kodi to help copy movies that are new to you\n\n";
		String footer = "\nPlease report issues to Matt Daniel";

		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);

		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("mediatool", header, options, footer, true);
			return;
		}

		/* User Options from the Command Line */
		boolean reportOnly = false;
		boolean doCopy = false;
		boolean copyDvds = false;
		File kodiLibFile = null;
		File kodiSourceFile = null;
		File destDir = null;
		boolean skipNonIMDB = false;
		boolean copyBetter = false;

		if (line.hasOption('h')) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("mediatool", header, options, footer, true);

			return;
		}

		if (line.hasOption('v')) {
			copyDvds = true;
		}

		if (line.hasOption('r')) {
			reportOnly = true;
		}

		if (line.hasOption('k')) {
			skipNonIMDB = true;
		}

		if (line.hasOption('c')) {
			doCopy = true;
		}

		if (line.hasOption('b')) {
			copyBetter = true;
		}

		if (line.hasOption('l')) {
			File file = new File(line.getOptionValue('l'));
			if (!file.exists()) {
				throw new RuntimeException(file.getAbsolutePath() + " not found!");
			}

			if (file.isDirectory()) {
				// TODO recursively look for database file
				throw new RuntimeException(file.getAbsolutePath() + " directories not supported yet for dest library");

			} else {
				kodiLibFile = file;
			}
		}

		if (line.hasOption('s')) {
			File file = new File(line.getOptionValue('s'));
			if (!file.exists()) {
				throw new RuntimeException(file.getAbsolutePath() + " not found!");
			}

			if (file.isDirectory()) {
				// TODO recursively look for database file
				throw new RuntimeException(
						file.getAbsolutePath() + " directories not supported yet for source library");

			} else {
				kodiSourceFile = file;
			}

		}

		if (line.hasOption('d')) {
			File file = new File(line.getOptionValue('d'));
			if (!file.exists()) {
				throw new RuntimeException(file.getAbsolutePath() + " not found!");
			}

			if (!file.isDirectory()) {
				// TODO recursively look for database file
				throw new RuntimeException(file.getAbsolutePath() + " directory must be specified as copy destination");

			} else {
				destDir = file;
			}

		} else {
			//do duplicate report
			System.out.println("---------Performing Dupe Report---------");
			new DupeReport().report(kodiLibFile);
			return;
		}

		System.out.println("reportOnly " + reportOnly);
		System.out.println("doCopy " + doCopy);
		System.out.println("copyDvds " + copyDvds);
		System.out.println("kodiLibFile " + kodiLibFile.getAbsolutePath());
		System.out.println("kodiSourceFile " + kodiSourceFile.getAbsolutePath());
		System.out.println("destDir " + destDir);
		System.out.println("skipNonIMDB " + skipNonIMDB);
		System.out.println("copyBetter " + copyBetter);

		Map<String, Movie> sourceMap = new HashMap<>();
		Map<String, Movie> libraryMap = new HashMap<>();
		Map<String, Movie> copyMap = new HashMap<>();
		Map<String, Movie> dupMap = new HashMap<>();

		int duplicateCount = 0;
		int newMovieCount = 0;
		int sourceMoviesCount = 0;
		int ignoreCount = 0;
		int copiedCount = 0;
		int filesCopiedCount = 0;
		int skippedCount = 0;

		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Connection connection = null;

		try {
			System.out.println("========================================================");
			System.out.println("Processing Kodi User Data from the movies to be imported");
			System.out.println("========================================================");

			connection = DriverManager.getConnection("jdbc:sqlite:" + kodiSourceFile);

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
				if (imdbId == null || imdbId.length() == 0) {
					if (skipNonIMDB) {
						System.out.println("<<< ignoring title not found in IMDB " + title + " (" + year + ") >>>");
						skippedCount++;
						continue;
					}
				}
				sourceMap.put(movie.getTitle() + movie.getYear(), movie);
				sourceMoviesCount++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}
		System.out.println("");
		System.out.println("========================================================");
		System.out.println("Processing Kodi User Data from the destination library");
		System.out.println("========================================================");

		try {
			// DriverManager.getConnection("jdbc:sqlite:/Users/mdaniel/Desktop/tmp/MyVideos99.db");
			connection = DriverManager.getConnection("jdbc:sqlite:" + kodiLibFile);

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select c00 as title, c07 as year, c09 as imdbid from movie");
			while (rs.next()) {
				String title = rs.getString(1);
				String year = rs.getString(2);
				String imdbId = rs.getString(3);
				Movie movie = new Movie();
				movie.setTitle(title);
				movie.setYear(Integer.parseInt(year));

				libraryMap.put(movie.getTitle() + movie.getYear(), movie);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
			}
		}
		System.out.println("========================================================");

		for (Entry<String, Movie> entry : sourceMap.entrySet()) {
			if (libraryMap.containsKey(entry.getKey())) {
				duplicateCount++;
				dupMap.put(entry.getKey(), entry.getValue());
			} else {
				newMovieCount++;
				copyMap.put(entry.getKey(), entry.getValue());
			}
		}

		if (copyBetter) {
			dupLoop: for (Entry<String, Movie> entry : dupMap.entrySet()) {
				Movie srcMovie = entry.getValue();
				Movie libMovie = libraryMap.get(entry.getKey());
				if (libMovie == null) {
					continue dupLoop;
				}
				if (srcMovie.getSizeInBytes() > libMovie.getSizeInBytes()) {
					copyMap.put(entry.getKey(), entry.getValue());
					System.out.println("Duplicate movie of larger file size to be copied "+srcMovie);
				}
			}
		}

		movieLoop: for (Entry<String, Movie> entry : copyMap.entrySet()) {

			Movie movie = entry.getValue();
			int ndx = 1;

			fileLoop: for (File srcFile : movie.getFiles()) {
				/*
				 * last bit is the file extension and file number identifier if
				 * multiple
				 */
				if (srcFile.isDirectory()) {

					if (copyDvds) {
						File destSubDir = new File(destDir, srcFile.getName());
						try {
							System.out.println(
									"copy dir " + srcFile.getAbsolutePath() + " to " + destSubDir.getAbsolutePath());
							if (doCopy && !reportOnly) {
								FileUtils.copyDirectory(srcFile, destSubDir, false);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						ignoreCount++;
					}
					continue movieLoop;

				} else {
					// System.out.println("NOT DIR "+srcFile);
				}

				String lastBit = "";
				try {
					lastBit = srcFile.getName().substring(srcFile.getName().lastIndexOf(".")).toLowerCase();
				} catch (Exception e) {
					System.out.println("couldn't determine lastbit for " + srcFile.getName());
				}
				if (movie.getFiles().size() > 1) {
					lastBit = " - CD" + ndx + lastBit;
					ndx++;
				}

				try {
					String title = movie.getTitle().replaceAll(" :", ",");
					title.replaceAll(":", ",");
					File destFile = new File(destDir, title + " (" + movie.getYear() + ")" + lastBit);
					System.out.println("copy " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());

					if (destFile.exists()) {
						continue fileLoop;
					}

					boolean copied = false;
					int retries = 2;
					while (!copied && retries > 0 && doCopy && !reportOnly) {
						try {
							FileUtils.copyFile(srcFile, destFile);
							filesCopiedCount++;
							copied = true;
							if (retries < 2) {
								System.out.println("Copy successfull on retry");
							}
							break;

						} catch (IOException io) {
							System.out.println("IO problem. " + (retries - 1) + " retries left.");
						}

						retries--;

					}

					if (!copied && !reportOnly) {
						System.out.println(
								"<<<FAILED>>> copy " + srcFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println();
			}
			copiedCount++;
		}
		System.out.println("========================================================");
		System.out.println("Source movies : " + sourceMoviesCount);
		System.out.println("Duplicates    : " + duplicateCount);
		System.out.println("New movies    : " + newMovieCount);
		System.out.println("Ignored       : " + ignoreCount);
		System.out.println("Library       : " + libraryMap.size());
		System.out.println("Movies Copied  : " + copiedCount);
		System.out.println("Files Copied   : " + filesCopiedCount);
		System.out.println("Skipped        : " + skippedCount);
	}

}
