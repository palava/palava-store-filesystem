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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A dummy {@link Process} implementation which does nothing.
 *
 * @since 1.2
 * @author Willi Schoenborn
 */
final class DummyProcess extends Process {
    
    private static final Process INSTANCE = new DummyProcess();
    
    private DummyProcess() {
        
    }

    @Override
    public void destroy() {

    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    public static Process getInstance() {
        return INSTANCE;
    }
    
}
