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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link FileSystemStore}.
 *
 * @author Willi Schoenborn
 */
public final class FileSystemStoreTest extends AbstractStoreTest {

    private final File directory = new File(System.getProperty("java.io.tmpdir"), "store");
    
    @Override
    public FileSystemStore unit() {
        try {
            return new FileSystemStore(directory);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    @Override
    protected Store unitWithGenerator(IdGenerator generator) {
        final FileSystemStore unit = unit();
        unit.setGenerator(generator);
        return unit;
    }
    
    /**
     * Runs after every test and deletes the store directory.
     * 
     * @throws IOException should not happen
     */
    @Before
    @After
    public void cleanup() throws IOException {
        FileUtils.deleteDirectory(directory);
    }
    
    /**
     * Tests whether {@link FileSystemStore#delete(String)} automatically deletes
     * empty parent directories.
     * 
     * @throws IOException should not happen 
     */
    @Test
    public void deleteEmptyDirectories() throws IOException {
        final Store unit = unit();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("willi.png");
        final String identifier = unit.create(stream);
        Assert.assertTrue(IOUtils.contentEquals(
            getClass().getClassLoader().getResourceAsStream("willi.png"), 
            unit.read(identifier))
        );
        unit.delete(identifier);
        Assert.assertTrue(directory.list().length == 0);
        Assert.assertTrue(directory.exists());
    }
    
    /**
     * Tests whether {@link FileSystemStore#delete(String)} keeps non empty directories
     * while deleting parent directories.
     * 
     * @throws IOException should not happen
     */
    @Test
    public void keepNonEmptyDirectories() throws IOException {
        final FileSystemStore unit = unit();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("willi.png");
        final String identifier = unit.create(stream);
        stream.close();
        final File target = unit.getFileIdentifier().toFile(directory, identifier);
        Assert.assertTrue(target.exists());
        Assert.assertTrue(directory.list().length > 0);
        final String otherIdentifier = identifier.substring(0, identifier.length() - 6) + "abcdef";
        unit.create(getClass().getClassLoader().getResourceAsStream("willi.png"), otherIdentifier);
        unit.delete(identifier);
        Assert.assertFalse(target.exists());
        Assert.assertTrue(directory.list().length > 0);
    }
    
}
