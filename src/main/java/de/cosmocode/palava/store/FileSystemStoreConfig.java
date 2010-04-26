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

/**
 * Constant holder class for file system store config key names.
 *
 * @author Willi Schoenborn
 */
final class FileSystemStoreConfig {

    public static final String PREFIX = StoreConfig.PREFIX + "filesystem.";
    
    public static final String DIRECTORY = PREFIX + "directory";
    
    public static final String FILE_IDENTIFIER = PREFIX + "fileIdentifier";
    
    private FileSystemStoreConfig() {
        
    }
    
}
