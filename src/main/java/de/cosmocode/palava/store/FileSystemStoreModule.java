package de.cosmocode.palava.store;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Binds the {@link Store} interface to {@link FileSystemStore}.
 *
 * @author Willi Schoenborn
 */
public final class FileSystemStoreModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(Store.class).to(FileSystemStore.class).in(Singleton.class);
    }

}
