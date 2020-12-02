package com.didiglobal.booster.instrument.sharedpreferences;

import com.sun.istack.internal.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.didiglobal.booster.instrument.sharedpreferences.io.IoUtils.close;

/**
 * @author neighbWang
 */
class SharedPreferencesLock implements ReadWriteLock, Lock {

    private final String mLockPath;
    private FileOutputStream mStream;
    private FileChannel mChannel;
    private java.nio.channels.FileLock mFileLock;
    private final Lock mReadLock;

    SharedPreferencesLock(final File spFile) {
        final File file = getLockFile(spFile);
        this.mLockPath = file.getAbsolutePath();
        this.mReadLock = new ReentrantReadWriteLock().readLock();
    }

    @Override
    public Lock readLock() {
        return mReadLock;
    }

    @Override
    public Lock writeLock() {
        return this;
    }

    @Override
    public void lock() {
        try {
            mStream = new FileOutputStream(mLockPath);
            mChannel = mStream.getChannel();
            mFileLock = mChannel.lock();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public void unlock() {
        if (mFileLock != null) {
            try {
                mFileLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        close(mChannel);
        close(mStream);
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Should not be called");
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
