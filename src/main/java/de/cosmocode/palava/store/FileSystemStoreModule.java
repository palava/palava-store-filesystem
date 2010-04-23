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
import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Binds the {@link Store} and the {@link ByteStore} interface to {@link FileSystemStore}.
 *
 * @author Willi Schoenborn
 */
public final class FileSystemStoreModule extends PrivateModule {

    private final Key<ByteStore> byteStoreKey;
    private final Key<Store> storeKey;
    
    private final String prefix;
    
    public FileSystemStoreModule() {
        this.byteStoreKey = Key.get(ByteStore.class);
        this.storeKey = Key.get(Store.class);
        this.prefix = null;
    }
    
    public FileSystemStoreModule(Class<? extends Annotation> annotationType, String prefix) {
        this.byteStoreKey = Key.get(ByteStore.class, annotationType);
        this.storeKey = Key.get(Store.class, annotationType);
        this.prefix = prefix;
    }
    
    @Override
    protected void configure() {
        if (StringUtils.isNotBlank(prefix)) {
            bind(File.class).annotatedWith(Names.named(FileSystemStoreConfig.DIRECTORY)).to(
                Key.get(File.class, Names.named(prefix + "." + FileSystemStoreConfig.DIRECTORY)));
        }
        
        bind(byteStoreKey).to(FileSystemStore.class).in(Singleton.class);
        bind(storeKey).to(byteStoreKey).in(Singleton.class);
        
        expose(byteStoreKey);
        expose(storeKey);
    }
    
}
