package io.jumpco.open.gradle.s3;

import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class S3Upload extends DefaultTask {

  @Optional
  @Input
  protected String bucket;
  @Optional
  @Input
  protected String awsAccessKeyId;
  @Optional
  @Input
  protected String awsSecretAccessKey;
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
  @Input
  private boolean compareContent = false;

  @Override
  public String getGroup() {
    return "s3";
  }

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

  public boolean isCompareContent() {
    return compareContent;
  }

  public void setCompareContent(boolean compareContent) {
    this.compareContent = compareContent;
  }

  @TaskAction
  public void task() throws InterruptedException, IOException {

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
        File file = getProject().file(getSourceDir());
        if (!file.exists()) {
          throw new GradleException("upload sourceDir:" + getSourceDir() + " not found");
        }
        if (isCompareContent() || !isOverwrite()) {
          Set<File> existing = new HashSet<>();
          List<File> uploadFiles = new ArrayList<>();
          List<File> sourceFiles = Files.walk(file.toPath(), Integer.MAX_VALUE).filter(f -> !Files.isDirectory(f))
              .map(Path::toFile)
              .collect(Collectors.toList());
          ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(getBucket()).withMaxKeys(2);
          ListObjectsV2Result result;
          do {
            result = util.getS3Client().listObjectsV2(req);
            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
              if (objectSummary.getKey().startsWith(keyPrefix)) {
                existing.add(new File(objectSummary.getKey()));
                getLogger().debug(getName() + ":listing:" + getBucket() + ":" + objectSummary.getKey());
              }
            }
            String token = result.getNextContinuationToken();
            req.setContinuationToken(token);
          } while (result.isTruncated());
          for (File sourceFile : sourceFiles) {
            File target = makeFile(file, sourceFile, keyPrefix);
            File portion = makeFile(file, sourceFile, null);
            if (existing.contains(target)) {
              if (isCompareContent()) {
                S3ObjectInputStream s3stream = util.getS3Client()
                    .getObject(getBucket(), target.getPath())
                    .getObjectContent();
                getLogger().info(getName() + ":upload:comparing:" + target + " -> " + sourceFile);
                if (IOUtils.contentEquals(s3stream.getDelegateStream(), new FileInputStream(sourceFile))) {
                  getLogger().info(getName() + ":upload:equals:skipping:" + target.getPath());
                } else {
                  getLogger().lifecycle(getName() + ":upload:different:adding:" + target.getPath());
                  util.getS3Client().putObject(getBucket(), target.getPath(), sourceFile);
                  getLogger().info(getName() + ":upload:completed:" + sourceFile.getPath());
                }
              } else if (overwrite) {
                getLogger().lifecycle(getName() + ":upload:exists:overwriting:" + target.getPath());
                util.getS3Client().putObject(getBucket(), target.getPath(), sourceFile);
                getLogger().info(getName() + ":upload:completed:" + sourceFile.getPath());
              }
            } else {
              getLogger().lifecycle(getName() + ":upload:new:" + target.getPath());
              util.getS3Client().putObject(getBucket(), target.getPath(), sourceFile);
              getLogger().info(getName() + ":upload:completed:" + sourceFile.getPath());
            }
          }
        } else {
          Transfer transfer = TransferManagerBuilder.standard()
              .withS3Client(util.getS3Client())
              .build()
              .uploadDirectory(getBucket(), getKeyPrefix(), file, true);
          ProgressListener listener = new S3Listener(transfer, getLogger());
          transfer.addProgressListener(listener);
          transfer.waitForCompletion();
        }

      } else {
        getLogger().lifecycle("testing:upload:" + getBucket());
      }
    } else if (getKey() != null && getFile() != null) {
      if (!S3BaseConfig.isTesting()) {
        File uploadFile = new File(getFile());
        if (!uploadFile.exists()) {
          throw new GradleException("upload file:" + getFile() + " not found");
        }
        if (util.getS3Client().doesObjectExist(getBucket(), getKey())) {
          if (isOverwrite()) {
            getLogger().lifecycle(getName() + ":" + getFile() + " → s3://" + getBucket() + "/" + getKey() + " with overwrite");
            util.getS3Client().putObject(getBucket(), getKey(), uploadFile);
          } else {
            if (isCompareContent()) {
              S3ObjectInputStream s3stream = util.getS3Client()
                  .getObject(getBucket(), getKey())
                  .getObjectContent();
              File target = new File(getKey());
              getLogger().info(getName() + ":upload:comparing:" + target + " -> " + uploadFile);
              if (IOUtils.contentEquals(s3stream.getDelegateStream(), new FileInputStream(uploadFile))) {
                getLogger().lifecycle(getName() + ":upload:equals:skipping:" + getKey());
              } else {
                getLogger().lifecycle(getName() + ":upload:" + getFile() + " → s3://" + getBucket() + "/" + getKey());
                util.getS3Client().putObject(getBucket(), getKey(), uploadFile);
              }
            }
            getLogger().warn(getName() + ":upload:s3://" + getBucket() + "/" + getKey() + " exists, not overwriting");
          }
        } else {
          getLogger().lifecycle(getName() + ":" + getFile() + " → s3://" + getBucket() + "/" + getKey());
          util.getS3Client().putObject(getBucket(), getKey(), uploadFile);
        }
      } else {
        getLogger().lifecycle(getName() + ":upload:" + getFile() + " → s3://" + getBucket() + "/" + getKey());
        getLogger().lifecycle("testing:upload:" + getName());
      }
    } else {
      throw new GradleException(
          "Invalid parameters: one of [key, file] or [keyPrefix, sourceDir] pairs must be specified for S3Upload");
    }
  }

  private File makeFile(File file, File sourceFile, String keyPrefix) {
    if (sourceFile.getPath().startsWith(file.getPath())) {
      String portion = sourceFile.getPath().substring(file.getPath().length());
      if (portion.startsWith(File.separator)) {
        portion = portion.substring(1);
      }
      return keyPrefix != null && keyPrefix.length() > 0 ? new File(keyPrefix, portion) : new File(portion);
    }
    return sourceFile;
  }
}