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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.name.Named;

/**
 * File system based implementation of the {@link ByteStore} interface.
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
final class FileSystemByteStore implements ByteStore {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemByteStore.class);

    private final File directory;
    
    public FileSystemByteStore(@Named(FileSystemStoreConfig.DIRECTORY) File directory) throws IOException {
        Preconditions.checkNotNull(directory, "Directory");
        FileUtils.forceMkdir(directory);
        this.directory = directory;
    }

    @Override
    public String create(ByteBuffer buffer) throws IOException {
        Preconditions.checkNotNull(buffer, "Buffer");
        final UUID uuid = UUID.randomUUID();
        
        final File file = getFile(uuid);
        Preconditions.checkState(!file.exists(), "File %s already present", file);
        final FileOutputStream output = FileUtils.openOutputStream(file);
        final FileChannel channel = output.getChannel();
        LOG.trace("Storing {} to {}", buffer, file);
        
        try {
            channel.write(buffer);
            output.flush();
        } finally {
            output.close();
        }
        
        return uuid.toString();
    }

    @Override
    public ByteBuffer read(String identifier) throws IOException {
        Preconditions.checkNotNull(identifier, "Identifier");
        final File file = getFile(identifier);
        LOG.trace("Reading file from {}", file);
        final FileChannel channel = FileUtils.openInputStream(file).getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
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
    
    private File getFile(UUID uuid) {
        return getFile(uuid.toString());
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
