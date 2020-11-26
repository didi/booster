package com.didiglobal.booster.instrument.sharedpreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.didiglobal.booster.instrument.sharedpreferences.io.IoUtils.close;

/**
 * @author neighbWang
 */
class SharedPreferencesLock {

    private final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();
    private final String mLockPath;
    private FileOutputStream mStream;
    private FileChannel mChannel;
    private java.nio.channels.FileLock mFileLock;

    SharedPreferencesLock(final File spFile) {
        final File file = getLockFile(spFile);
        this.mLockPath = file.getAbsolutePath();
    }

    Lock readLock() {
        return mReadWriteLock.readLock();
    }

    SharedPreferencesLock writeLock() throws IOException {
        mStream = new FileOutputStream(mLockPath);
        mChannel = mStream.getChannel();
        mFileLock = mChannel.lock();
        return this;
    }

    void unlock() {
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
