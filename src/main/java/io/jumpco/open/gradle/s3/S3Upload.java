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

public class S3Upload extends DefaultTask {

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
    private String sourceDir;

    @Input
    private boolean overwrite = false;

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

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @TaskAction
    public void task() throws InterruptedException {

        if (getBucket() == null) {
            throw new GradleException("Invalid parameters: [bucket] was not provided and/or a default was not set");
        }
        SS3Util util = new SS3Util(getProject(), getBucket(), getAwsAccessKeyId(), getAwsSecretAccessKey());
        if (getKeyPrefix() != null && getSourceDir() != null) {
            if (getKey() != null || getFile() != null) {
                throw new GradleException("Invalid parameters: [key, file] are not valid for S3 Upload directory");
            }
            getLogger().lifecycle(getName() + ":directory:" + getProject().file(getSourceDir()) + " → s3://" + getBucket() + "/" + getKeyPrefix());
            if (!S3BaseConfig.isTesting()) {
                File file = getProject().file(sourceDir);
                if (!file.exists()) {
                    throw new GradleException("upload sourceDir:" + sourceDir + " not found");
                }
                Transfer transfer = TransferManagerBuilder.standard().withS3Client(util.getS3Client()).build()
                        .uploadDirectory(getBucket(), getKeyPrefix(), file, true);


                ProgressListener listener = new S3Listener(transfer, getLogger());
                transfer.addProgressListener(listener);
                transfer.waitForCompletion();
            } else {
                getLogger().lifecycle("testing:upload:" + getBucket());
            }
        } else if (getKey() != null && getFile() != null) {
            if (!S3BaseConfig.isTesting()) {
                File uploadFile = new File(this.file);
                if (!uploadFile.exists()) {
                    throw new GradleException("upload file:" + this.file + " not found");
                }
                if (util.getS3Client().doesObjectExist(getBucket(), getKey())) {
                    if (isOverwrite()) {
                        getLogger().lifecycle(getName() + ":" + getFile() + " → s3://" + getBucket() + "/" + getKey() + " with overwrite");
                        util.getS3Client().putObject(bucket, key, uploadFile);
                    } else {
                        getLogger().warn("s3://" + getBucket() + "/" + getKey() + " exists, not overwriting");
                    }
                } else {
                    getLogger().lifecycle(getName() + ":" + getFile() + " → s3://" + getBucket() + "/" + getKey());
                    util.getS3Client().putObject(bucket, key, uploadFile);
                }
            } else {
                getLogger().lifecycle(getName() + ":" + getFile() + " → s3://" + getBucket() + "/" + getKey());
                getLogger().lifecycle("testing:upload:" + getName());
            }
        } else {
            throw new GradleException("Invalid parameters: one of [key, file] or [keyPrefix, sourceDir] pairs must be specified for S3Upload");
        }
    }
}