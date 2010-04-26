/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
