package com.didiglobal.booster.instrument.sharedpreferences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import static com.didiglobal.booster.instrument.sharedpreferences.io.IoUtils.close;

/**
 * @author neighbWang
 */
class SharedPreferencesLock {

    private final String mLockPath;
    private FileOutputStream mStream;
    private FileChannel mChannel;
    private java.nio.channels.FileLock mFileLock;

    SharedPreferencesLock(final File spFile) {
        final File file = getLockFile(spFile);
        this.mLockPath = file.getAbsolutePath();
    }

    SharedPreferencesLock lock() throws IOException {
        mStream = new FileOutputStream(mLockPath);
        mChannel = mStream.getChannel();
        mFileLock = mChannel.lock();
        return this;
    }

    void release() {
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
        final String name = file.getName();
        final File lockFile = new File(file.getParent(), name + ".lock");
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lockFile;
    }
}
