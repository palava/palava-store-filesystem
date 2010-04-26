package de.cosmocode.palava.store;

import java.io.File;

/**
 * Default implementation of the {@link FileIdentifier} interface.
 *
 * @author Willi Schoenborn
 */
final class DefaultFileIdentifier implements FileIdentifier {

    @Override
    public File toFile(File directory, String identifier) {
        return new File(directory, identifier.replace("-", File.separator));
    }
    
    @Override
    public String toIdentifier(File directory, File file) {
        return file.getPath().replace(directory.getPath(), "").substring(1).replace(File.separator, "-");
    }
    
}
