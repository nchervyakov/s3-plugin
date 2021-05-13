package io.jumpco.open.gradle.s3;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class S3DownloadConfig extends S3BaseConfig {

    public S3DownloadConfig(String name, ObjectFactory objectFactory) {
        super(name, objectFactory);
        this.destDir = objectFactory.property(String.class);
        this.skipError = objectFactory.property(Boolean.class);
    }


    private Property<String> destDir;
    private Property<Boolean> skipError;

    public Property<String> getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir.set(destDir);
    }

    public Property<Boolean> getSkipError() {
        return skipError;
    }

    public void setSkipError(Boolean skipError) {
        this.skipError.set(skipError);
    }

    @Override
    public String toString() {
        return "S3DownloadExtension{" +
                "destDir='" + destDir + '\'' +
                "skipError='" + skipError + '\'' +
                super.toString() +
                '}';
    }
}
