package io.jumpco.open.gradle.s3;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class S3UploadConfig extends S3BaseConfig {

  private Property<String> sourceDir;
  private Property<Boolean> overwrite;
  private Property<Boolean> compareContent;

  public S3UploadConfig(String name, ObjectFactory objectFactory) {
    super(name, objectFactory);

    this.overwrite = objectFactory.property(Boolean.class);
    this.overwrite.set(false);

    this.compareContent = objectFactory.property(Boolean.class);
    this.compareContent.set(false);

    this.sourceDir = objectFactory.property(String.class);
  }

  public Property<String> getSourceDir() {
    return sourceDir;
  }

  public void setSourceDir(String sourceDir) {
    this.sourceDir.set(sourceDir);
  }

  public Property<Boolean> getOverwrite() {
    return overwrite;
  }

  public void setOverwrite(Boolean overwrite) {
    this.overwrite.set(overwrite);
  }

  public Property<Boolean> getCompareContent() {
    return compareContent;
  }

  public void setCompareContent(Boolean compareContent) {
    this.compareContent.set(compareContent);
  }

  @Override
  public String toString() {
    return "S3UploadExtension{" +
        "sourceDir='" + sourceDir + '\'' +
        ", overwrite=" + overwrite +
        ", compareContent=" + compareContent +
        super.toString() +
        '}';
  }
}
