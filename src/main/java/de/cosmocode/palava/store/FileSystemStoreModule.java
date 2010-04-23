/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
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
                Key.get(File.class, Names.named(prefix + FileSystemStoreConfig.DIRECTORY)));
        }
        
        bind(byteStoreKey).to(FileSystemStore.class).in(Singleton.class);
        bind(storeKey).to(byteStoreKey).in(Singleton.class);
    }
    
}
