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
import com.google.inject.Inject;
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
public final class FileSystemStore extends AbstractByteStore implements ByteStore {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemStore.class);
    
    private final File directory;
    
    private IdGenerator generator = new UUIDGenerator();
    
    private FileIdentifier fileIdentifier = new DefaultFileIdentifier();

    private final Function<File, String> toIdentifier = new Function<File, String>() {
        
        @Override
        public String apply(File from) {
            return fileIdentifier.toIdentifier(directory, from);
        }
        
    };
    
    @Inject
    FileSystemStore(@Named(FileSystemStoreConfig.DIRECTORY) File directory) throws IOException {
        Preconditions.checkNotNull(directory, "Directory");
        FileUtils.forceMkdir(directory);
        this.directory = directory;
    }
    
    @Inject(optional = true)
    void setGenerator(@Named(StoreConfig.ID_GENERATOR) IdGenerator generator) {
        this.generator = Preconditions.checkNotNull(generator, "Generator");
    }
    
    @Inject(optional = true)
    void setFileIdentifier(@Named(FileSystemStoreConfig.FILE_IDENTIFIER) FileIdentifier identifier) {
        this.fileIdentifier = identifier;
    }
    
    @Override
    public String create(InputStream stream) throws IOException {
        Preconditions.checkNotNull(stream, "Stream");
        final String uuid = generator.generate();
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
        Preconditions.checkState(file.exists(), "%s does not exist", file);
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
        Preconditions.checkState(file.exists(), "%s does not exist", file);
        LOG.trace("Removing {} from store", file);
        FileUtils.forceDelete(file);
        deleteEmptyParent(file.getParentFile());
    }
    
    /**
     * Provides a file pointing to the target as specified by
     * the given identifier.
     * 
     * @param identifier the file identifier
     * @return a file (may not exist)
     */
    public File getFile(String identifier) {
        return fileIdentifier.toFile(directory, identifier);
    }
    
    /**
     * Reads a file from this store.
     * 
     * @param identifier the identifier of the binary data being retrieved
     * @return the file associated with the given identifier
     * @throws IOException if file does not exist
     */
    public File readFile(String identifier) throws IOException {
        Preconditions.checkNotNull(identifier, "Identifier");
        final File file = getFile(identifier);
        if (file.exists()) {
            return file;
        } else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }
    
    private void deleteEmptyParent(File file) throws IOException {
        Preconditions.checkArgument(file.isDirectory(), "%s has to be a directory", file);
        
        // do not delete configured directory
        if (directory.equals(file)) return;
        
        if (file.list().length > 0) {
            LOG.trace("Keeping non empty directory {}", file);
            return;
        }
        
        LOG.trace("Deleting empty directory {}", file);
        FileUtils.deleteDirectory(file);
        deleteEmptyParent(file.getParentFile());
    }

}
