package io.jumpco.open.gradle.s3;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class S3UploadConfig extends S3BaseConfig {

    public S3UploadConfig(String name, ObjectFactory objectFactory) {
        super(name, objectFactory);
        this.overwrite = objectFactory.property(Boolean.class);
        this.overwrite.set(false);
        this.sourceDir = objectFactory.property(String.class);
    }


    private Property<String> sourceDir;

    private Property<Boolean> overwrite;


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

    @Override
    public String toString() {
        return "S3UploadExtension{" +
                "sourceDir='" + sourceDir + '\'' +
                ", overwrite=" + overwrite +
                super.toString() +
                '}';
    }
}
