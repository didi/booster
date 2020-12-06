package com.didiglobal.booster.instrument.sharedpreferences;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author neighbWang
 */
class SharedPreferencesLock implements ReadWriteLock {

    private final Lock mReadLock;
    private final Lock mWriteLock;

    SharedPreferencesLock(final File spFile) {
        this.mReadLock = new ReentrantReadWriteLock().readLock();
        this.mWriteLock = new WriteLock(getLockFile(spFile));
    }

    @Override
    public Lock readLock() {
        return mReadLock;
    }

    @Override
    public Lock writeLock() {
        return mWriteLock;
    }

    private static File getLockFile(final File file) {
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        final File lockFile = new File(parent, file.getName() + ".lock");
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (IOException ignore) {
            }
        }
        return lockFile;
    }
}
