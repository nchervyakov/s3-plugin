package io.jumpco.open.gradle.s3;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class S3Download extends DefaultTask {
    @Override
    public String getGroup() {
        return "s3";
    }

    @Optional
    @Input
    protected String bucket;

    @Optional
    @Input
    protected String awsAccessKeyId;

    @Optional
    @Input
    protected String awsSecretAccessKey;

    S3Extension getExt() {
        return getProject().getExtensions().findByType(S3Extension.class);
    }


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

    public String getBucket() {
        if (bucket == null) {
            return getExt().getBucket();
        }
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAwsAccessKeyId() {
        if (awsAccessKeyId == null) {
            return getExt().getAwsAccessKeyId();
        }
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        if (awsSecretAccessKey == null) {
            return getExt().getAwsSecretAccessKey();
        }
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

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

        Transfer transfer = null;

        if (getBucket() == null) {
            throw new GradleException("Invalid parameters: [bucket] was not provided and/or a default was not set");
        }
        SS3Util util = new SS3Util(getProject(), getBucket(), getAwsAccessKeyId(), getAwsSecretAccessKey());

        if (keyPrefix != null && destDir != null) {
            if (key != null || file != null) {
                throw new GradleException("Invalid parameters: [key, file] are not valid for S3Download recursive");
            }
            getLogger().lifecycle(getName() + ":directory:s3://" + getBucket() + "/" + keyPrefix + " → " + destDir + "/");
            if (!S3BaseConfig.isTesting()) {
                transfer = TransferManagerBuilder.standard().withS3Client(util.getS3Client()).build()
                        .downloadDirectory(getBucket(), keyPrefix, getProject().file(destDir));
            }
        } else if (key != null && file != null) {
            if (keyPrefix != null || destDir != null) {
                throw new GradleException("Invalid parameters: [keyPrefix, destDir] are not valid for S3 Download single file");
            }
            getLogger().lifecycle(getName() + ":file:s3://" + getBucket() + "/" + key + " → " + file);
            if (!S3BaseConfig.isTesting()) {
                File f = new File(file);
                if (f.getParentFile() != null && !f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                transfer = TransferManagerBuilder.standard().withS3Client(util.getS3Client()).build()
                        .download(getBucket(), key, f);
            }
        } else {
            throw new GradleException("Invalid parameters: one of [key, file] or [keyPrefix, destDir] pairs must be specified for S3 Download");
        }
        if (!S3BaseConfig.isTesting()) {
            if (transfer == null) {
                throw new GradleException("Expected transfer");
            }
            ProgressListener listener = new S3Listener(transfer, getLogger());
            transfer.addProgressListener(listener);
            transfer.waitForCompletion();
        } else {
            getLogger().lifecycle(getName() + "testing:" + getBucket());
        }
    }
}