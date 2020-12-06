package com.didiglobal.booster.instrument.sharedpreferences;

import com.didiglobal.booster.instrument.sharedpreferences.io.IoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author neighbWang
 */
final class WriteLock implements Lock, AutoCloseable {

    private final File mFile;

    private FileLock mFileLock;

    WriteLock(final File file) {
        this.mFile = file;
    }

    @Override
    public void lock() {
        tryLock();
    }

    @Override
    public void lockInterruptibly() {
    }

    @Override
    public boolean tryLock() {
        FileChannel fc = null;
        try {
            fc  = FileChannel.open(mFile.toPath(), StandardOpenOption.WRITE);
            return  null != (mFileLock = fc.lock());
        } catch (IOException e) {
            IoUtils.close(fc);
            if (mFileLock != null) {
                try {
                    mFileLock.release();
                } catch (IOException ignore) {
                }
            }
            return false;
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public void unlock() {
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override
    public void close() throws IOException {
        mFile.delete();
        if (mFileLock != null) {
            mFileLock.release();
        }
    }
}
