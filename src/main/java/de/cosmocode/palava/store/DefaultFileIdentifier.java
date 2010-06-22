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
 * Default implementation of the {@link FileIdentifier} interface.
 *
 * @author Willi Schoenborn
 */
public final class DefaultFileIdentifier implements FileIdentifier {

    @Override
    public File toFile(File directory, String identifier) {
        final StringBuilder builder = new StringBuilder(identifier);
        
        for (int i = 3; i < builder.length(); i += 4) {
            builder.insert(i, File.separator);
        }

        return new File(directory, builder.toString());
    }
    
    @Override
    public String toIdentifier(File directory, File file) {
        return file.getPath().replace(directory.getPath(), "").substring(1).replace(File.separator, "");
    }
    
}
