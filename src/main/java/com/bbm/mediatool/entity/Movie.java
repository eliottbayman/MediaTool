package com.bbm.mediatool.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Movie {

	private String title;

	private Integer year;

	private String rated;

	private String released;

	private String runtime;
	
	private String imdbId;

	private List<File> files = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getRated() {
		return rated;
	}

	public void setRated(String rated) {
		this.rated = rated;
	}

	public String getReleased() {
		return released;
	}

	public void setReleased(String released) {
		this.released = released;
	}

	public String getRuntime() {
		return runtime;
	}

	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> file) {
		this.files = file;
	}

	/**
	 * Multiple files may be present in the string.
	 * stack:/Volumes/UNTITLED/COLLECTIONS/Lord of the Rings (3)/The Lord of the
	 * Rings -- The Fellowship of the Ring (Extended Edition)/Lord of the Rings
	 * - The Fellowship of the Ring CD1.avi , /Volumes/UNTITLED/COLLECTIONS/Lord
	 * of the Rings (3)/The Lord of the Rings -- The Fellowship of the Ring
	 * (Extended Edition)/Lord of the Rings - The Fellowship of the Ring CD2.avi
	 * , /Volumes/UNTITLED/COLLECTIONS/Lord of the Rings (3)/The Lord of the
	 * Rings -- The Fellowship of the Ring (Extended Edition)/Lord of the Rings
	 * - The Fellowship of the Ring CD3.avi
	 * 
	 * @param fileStr
	 */
	public void setFiles(String fileStr) {
		if (fileStr.startsWith("stack:")) {
			StringBuffer buf = new StringBuffer(fileStr);
			int i = 0;
			while (i >= 0) {
				i = buf.toString().indexOf(" , /");
				if (i == -1) {
//					System.out.println("Unhandled :"+fileStr);
//					return;
					i = buf.toString().indexOf(" , smb://");
				} 
				
				if (i == -1) {
					System.out.println("Unhandled :"+fileStr);
					return;
				}
				
				String s = buf.toString().substring(0, i).trim();
				if (s.startsWith("stack:")) {
					s = s.substring(6);
				}
				buf.delete(0, i + 3);
				buf.trimToSize();
				// System.out.println("->"+s+"<-");
				files.add(new File(s));
				// System.out.println("<<<"+buf);
				i = buf.toString().indexOf(" , /");
				if (i < 0) {
					if (buf.toString().startsWith("stack:")) {
						s = buf.toString().substring(6);
					}
					s = buf.toString().trim();

					/*
					 * Hack to fix cases where double commas get into the file
					 * name of subsequent files
					 */
					if (s.contains(",,")) {
						String hackedStr = s.replace(",,", ",");
						files.add(new File(hackedStr));
					} else {
						files.add(new File(s));
					}
				}
			}
		} else {
			files.add(new File(fileStr));
		}
	}
	
	

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	@Override
	public String toString() {
		StringBuffer fileStr = new StringBuffer();
		for (File file : this.files) {
			fileStr.append("\n" + file.getAbsolutePath());
		}
		return "Movie [movieTitle=" + title + ", year=" + year + " files=" + fileStr + "]";
	}

	public long getSizeInBytes() {
		long count = 0;
		for (File f : this.files) {
			if (!f.isDirectory()) {
				count += f.length();
			}
		}
		return count;
	}

	/**
	 * Testing purposes
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		String t1 = "stack:///Volumes/P-PARTOUT D/MOVIES FROM KRILL/AMADEUS/Amadeus CD 1.avi , /Volumes/P-PARTOUT D/MOVIES FROM KRILL/AMADEUS/Amadeus CD 2.avi";
		String t2 = "stack:///Volumes/P-PARTOUT D/MOVIES FROM KRILL/Weight of Water, The/The.Weight.of.Water-AC3.CD1.avi , /Volumes/P-PARTOUT D/MOVIES FROM KRILL/Weight of Water,, The/The.Weight.of.Water-AC3.CD2.avi";
		// String t2 = "stack:///Users/mdaniel/Desktop/Weight of Water,
		// The/The.Weight.of.Water-AC3.CD1.avi , /Volumes/P-PARTOUT D/MOVIES
		// FROM KRILL/Weight of Water,, The/The.Weight.of.Water-AC3.CD2.avi";

		String t3 = "/Volumes/P-PARTOUT D/MOVIES FROM VAGABOND/O Brother, Where Art Thou/O Brother, Where Art Thou (2000).avi";

		Movie m1 = new Movie();
		m1.setTitle("Amadeus");
		m1.setYear(1989);
		m1.setFiles(t1);

		Movie m2 = new Movie();
		m2.setTitle("Weight of Water");
		m2.setYear(1999);
		m2.setFiles(t2);

		Movie m3 = new Movie();
		m3.setTitle("O Brother, Where Art Thou");
		m3.setYear(2000);
		m3.setFiles(t3);

		System.out.println(m1);
		System.out.println(m2);
		System.out.println(m3);
	}
}
