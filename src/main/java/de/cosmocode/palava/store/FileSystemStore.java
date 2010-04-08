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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.inject.name.Named;

/**
 * File system based implementation of the {@link Store} interface.
 * 
 * <p>
 *   This implementation uses {@link UUID} to generate unique identifiers.
 *   {@link InputStream}s will be saved as files in sub-directories.
 *   Each sub directory represents on segment of the generated uuid, splitted
 *   by the dash signs.
 * </p>
 *
 * @author Willi Schoenborn
 */
final class FileSystemStore extends AbstractByteStore implements ByteStore {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStore.class);
    
    private final File directory;

    private final Function<File, String> toIdentifier = new Function<File, String>() {
        
        @Override
        public String apply(File from) {
            return from.getPath().replace(directory.getPath(), "").substring(1).replace(File.separator, "-");
        }
        
    };
    
    public FileSystemStore(@Named(FileSystemStoreConfig.DIRECTORY) File directory) throws IOException {
        Preconditions.checkNotNull(directory, "Directory");
        FileUtils.forceMkdir(directory);
        this.directory = directory;
    }
    
    @Override
    public String create(InputStream stream) throws IOException {
        Preconditions.checkNotNull(stream, "Stream");
        final String uuid = UUID.randomUUID().toString();
        create(stream, uuid);
        return uuid;
    }
    
    @Override
    public void create(InputStream stream, String identifier) throws IOException {
        Preconditions.checkNotNull(stream, "Stream");
        final File file = getFile(identifier);
        Preconditions.checkState(!file.exists(), "File %s is already present", file);
        final OutputStream output = FileUtils.openOutputStream(file);
        LOG.trace("Storing {} to {}", stream, file);
        
        try {
            IOUtils.copy(stream, output);
            output.flush();
        } finally {
            output.close();
        }
    }
    
    @Override
    public ByteBuffer view(String identifier) throws IOException {
        Preconditions.checkNotNull(identifier, "Identifier");
        final File file = getFile(identifier);
        LOG.trace("Reading file from {}", file);
        final FileChannel channel = new RandomAccessFile(file, "r").getChannel();
        return channel.map(MapMode.READ_ONLY, 0, channel.size());
    }
    
    @Override
    public Set<String> list() throws IOException {
        final IOFileFilter fileFilter = FileFilterUtils.fileFileFilter();
        final IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        @SuppressWarnings("unchecked")
        final Collection<File> files = FileUtils.listFiles(directory, fileFilter, directoryFilter);
        return Sets.newHashSet(Collections2.transform(files, toIdentifier));
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     *   Recursively deletes empty parent directories.
     * </p>
     */
    @Override
    public void delete(String identifier) throws IOException {
        Preconditions.checkNotNull(identifier, "Identifier");
        final File file = getFile(identifier);
        
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("%s does not exist", file));
        }
        
        LOG.trace("Removing {} from store", file);
        FileUtils.forceDelete(file);
        
        deleteEmptyParent(file.getParentFile());
    }
    
    private File getFile(String identifier) {
        return new File(directory, identifier.replace("-", File.separator));
    }
    
    private void deleteEmptyParent(File file) throws IOException {
        Preconditions.checkArgument(file.isDirectory(), "%s has to be a directory", file);
        
        if (file.list().length > 0) {
            LOG.trace("Keeping non empty directory {}", file);
            return;
        }
        
        if (file.equals(directory)) return;
    
        LOG.trace("Deleting empty directory {}", file);
        FileUtils.deleteDirectory(file);
        deleteEmptyParent(file.getParentFile());
    }

}
