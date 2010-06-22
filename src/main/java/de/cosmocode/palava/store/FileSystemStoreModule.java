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

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import de.cosmocode.palava.core.inject.AbstractRebindModule;
import de.cosmocode.palava.core.inject.Config;
import de.cosmocode.palava.core.inject.RebindModule;

/**
 * Binds the {@link Store} and the {@link ByteStore} interface to {@link FileSystemStore}.
 *
 * @author Willi Schoenborn
 */
public final class FileSystemStoreModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(FileSystemStore.class).in(Singleton.class);
        binder.bind(ByteStore.class).to(FileSystemStore.class).in(Singleton.class);
        binder.bind(Store.class).to(ByteStore.class).in(Singleton.class);
    }
    
    /**
     * Equivalent to {@link #annotatedWith(Annotation, String)} using
     * {@code Named.names(name), name}.
     * 
     * @param name the name being used
     * @return a module which rebinds configuration and exposed keys using the
     *         specified key
     */
    public static RebindModule named(String name) {
        Preconditions.checkNotNull(name, "Name");
        return annotatedWith(Names.named(name), name);
    }
    
    /**
     * Rebinds all configuration entries using the specified prefix for configuration
     * keys and the supplied annoation for key rebindings.
     * 
     * @param annotation the new binding annotation
     * @param prefix the prefix
     * @return a module which rebinds all required settings
     */
    public static RebindModule annotatedWith(Annotation annotation, String prefix) {
        Preconditions.checkNotNull(annotation, "Annotation");
        Preconditions.checkNotNull(prefix, "Prefix");
        return new AnnotatedInstanceModule(annotation, prefix);
    }
    
    /**
     * A {@link RebindModule} which uses a name to rebind using {@link Named}.
     *
     * @author Willi Schoenborn
     */
    private static final class AnnotatedInstanceModule extends AbstractRebindModule {
        
        private final Annotation key;
        private final Config config;
        
        private AnnotatedInstanceModule(Annotation annotation, String prefix) {
            this.key = annotation;
            this.config = new Config(prefix);
        }

        @Override
        protected void configuration() {
            bind(File.class).annotatedWith(Names.named(FileSystemStoreConfig.DIRECTORY)).to(
                Key.get(File.class, Names.named(config.prefixed(FileSystemStoreConfig.DIRECTORY))));
        }
        
        @Override
        protected void optionals() {
            bind(IdGenerator.class).annotatedWith(Names.named(StoreConfig.ID_GENERATOR)).to(
                Key.get(IdGenerator.class, Names.named(config.prefixed(StoreConfig.ID_GENERATOR)))
            ).in(Singleton.class);
            
            bind(FileIdentifier.class).annotatedWith(Names.named(FileSystemStoreConfig.FILE_IDENTIFIER)).to(
                Key.get(FileIdentifier.class, Names.named(config.prefixed(FileSystemStoreConfig.FILE_IDENTIFIER)))
            ).in(Singleton.class);
            
            bind(String.class).annotatedWith(Names.named(FileSystemStoreConfig.UNIX_OWNER)).to(
                Key.get(String.class, Names.named(config.prefixed(FileSystemStoreConfig.UNIX_OWNER))));
            
            bind(String.class).annotatedWith(Names.named(FileSystemStoreConfig.UNIX_PERMISSIONS)).to(
                Key.get(String.class, Names.named(config.prefixed(FileSystemStoreConfig.UNIX_PERMISSIONS))));
        }
    
        @Override
        protected void bindings() {
            bind(FileSystemStore.class).annotatedWith(key).to(FileSystemStore.class).in(Singleton.class);
            bind(ByteStore.class).annotatedWith(key).to(Key.get(FileSystemStore.class, key)).in(Singleton.class);
            bind(Store.class).annotatedWith(key).to(Key.get(ByteStore.class, key)).in(Singleton.class);
        }

        @Override
        protected void expose() {
            expose(FileSystemStore.class).annotatedWith(key);
            expose(ByteStore.class).annotatedWith(key);
            expose(Store.class).annotatedWith(key);
        }
        
    }

    /**
     * Rebinds all configuration entries using the specified name as prefix for configuration
     * keys and the supplied annoation for key rebindings.
     * 
     * @param annotationType the new binding annotation
     * @param prefix the prefix
     * @return a module which rebinds all required settings
     */
    public static RebindModule annotatedWith(Class<? extends Annotation> annotationType, String prefix) {
        Preconditions.checkNotNull(annotationType, "AnnotationType");
        Preconditions.checkNotNull(prefix, "Prefix");
        return new AnnotatedModule(annotationType, prefix);
    }
    
    /**
     * A {@link RebindModule} which uses {@link Key#get(java.lang.reflect.Type, Annotation)} to rebind.
     *
     * @author Willi Schoenborn
     */
    private static final class AnnotatedModule extends AbstractRebindModule {
        
        private final Class<? extends Annotation> key;
        private final Config config;
        
        private AnnotatedModule(Class<? extends Annotation> key, String prefix) {
            this.key = key;
            this.config = new Config(prefix);
        }

        @Override
        protected void configuration() {
            bind(File.class).annotatedWith(Names.named(FileSystemStoreConfig.DIRECTORY)).to(
                Key.get(File.class, Names.named(config.prefixed(FileSystemStoreConfig.DIRECTORY))));
        }
        
        @Override
        protected void optionals() {
            bind(IdGenerator.class).annotatedWith(Names.named(StoreConfig.ID_GENERATOR)).to(
                Key.get(IdGenerator.class, Names.named(config.prefixed(StoreConfig.ID_GENERATOR)))
            ).in(Singleton.class);
            
            bind(FileIdentifier.class).annotatedWith(Names.named(FileSystemStoreConfig.FILE_IDENTIFIER)).to(
                Key.get(FileIdentifier.class, Names.named(config.prefixed(FileSystemStoreConfig.FILE_IDENTIFIER)))
            ).in(Singleton.class);
        }
    
        @Override
        protected void bindings() {
            bind(FileSystemStore.class).annotatedWith(key).to(FileSystemStore.class).in(Singleton.class);
            bind(ByteStore.class).annotatedWith(key).to(Key.get(FileSystemStore.class, key)).in(Singleton.class);
            bind(Store.class).annotatedWith(key).to(Key.get(ByteStore.class, key)).in(Singleton.class);
        }

        @Override
        protected void expose() {
            expose(FileSystemStore.class).annotatedWith(key);
            expose(ByteStore.class).annotatedWith(key);
            expose(Store.class).annotatedWith(key);
        }
        
    }
    
}
