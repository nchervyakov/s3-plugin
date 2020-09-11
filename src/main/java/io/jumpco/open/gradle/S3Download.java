package io.jumpco.open.gradle;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class S3Download extends S3Task {

    @Optional
    @Input
    private String key;

    @Optional
    @Input
    private String file;

    @Optional
    @Input
    private String keyPrefix;

    @Optional
    @Input
    private String destDir;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    @TaskAction
    public void task() throws InterruptedException {

        Transfer transfer;

        if (getBucket() == null) {
            throw new GradleException("Invalid parameters: [bucket] was not provided and/or a default was not set");
        }

        // directory download
        if (keyPrefix != null && destDir != null) {
            if (key != null || file != null) {
                throw new GradleException("Invalid parameters: [key, file] are not valid for S3Download recursive");
            }
            if (getLogger().isQuietEnabled()) {
                getLogger().quiet("S3 Download recursive s3://" + getBucket() + "/" + keyPrefix + " → " + destDir + "/");
            }
            transfer = TransferManagerBuilder.standard().withS3Client(getS3Client()).build()
                    .downloadDirectory(getBucket(), keyPrefix, getProject().file(destDir));
        } else if (key != null && file != null) {
            if (keyPrefix != null || destDir != null) {
                throw new GradleException("Invalid parameters: [keyPrefix, destDir] are not valid for S3 Download single file");
            }
            if (getLogger().isQuietEnabled()) {
                getLogger().quiet("S3 Download s3://" + getBucket() + "/" + key + " → " + file);
            }
            File f = new File(file);
            f.getParentFile().mkdirs();
            transfer = TransferManagerBuilder.standard().withS3Client(getS3Client()).build()
                    .download(getBucket(), key, f);
        } else {
            throw new GradleException("Invalid parameters: one of [key, file] or [keyPrefix, destDir] pairs must be specified for S3 Download");
        }

        ProgressListener listener = new S3Listener(transfer, getLogger());
        transfer.addProgressListener(listener);
        transfer.waitForCompletion();
    }
}