package de.cosmocode.palava.store;

import java.io.File;

/**
 * A simple converter used by the {@link FileSystemStore} to produces
 * {@link File}s from {@link String}s and vice versa.
 *
 * @author Willi Schoenborn
 */
public interface FileIdentifier {

    /**
     * Produces a File from the given identifier.
     * 
     * @param directory the base directory
     * @param identifier the identifier
     * @return a file
     */
    File toFile(File directory, String identifier);
    
    /**
     * Produces a String from the given file.
     * 
     * @param directory the base directory
     * @param file the file
     * @return a string
     */
    String toIdentifier(File directory, File file);
    
}
