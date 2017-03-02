# MediaTool
Import movies new to you using Kodi

This is a Java program runnable from the command line of any OS. It has no GUI and is not trivial to use without some
understanding of Kodi Media Center.  It is not a Kodi plugin.  It is a tool to assist in consolidating ones own collection
of Movies.  No streaming is performed by this code.  It only copies movie files that are already in your possession.

Use Maven and Java 8 or later to build and run an executable jar file. 

usage: mediatool [-b] [-c] [-d <arg>] [-h] [-k] [-l <arg>] [-r] [-s <arg>] [-v]

Use Kodi to help copy movies that are new to you

 -b,--copyBetter          Copy duplicate movies that are probably better
                          quality
 -c,--copy                Attempt to copy the new movies.
 -d,--destination <arg>   Directory location where movies will be copied
                          to
 -h,--help                print this message
 -k,--skipNonIMDB         skip movies that are not found by IMDB.com
 -l,--library <arg>       File location of Kodi user data for the existing
                          library
 -r,--report              Report what movies will be copied. Copies
                          nothing - just report.
 -s,--source <arg>        File location of Kodi user data for the movies
                          to be copied
 -v,--dvd                 Include movies that appear to be a folder of DVD
                          format. These can be huge.
