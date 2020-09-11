package io.jumpco.open.gradle.s3;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class S3DownloadConfig extends S3BaseConfig {

    public S3DownloadConfig(String name, ObjectFactory objectFactory) {
        super(name, objectFactory);
        this.destDir = objectFactory.property(String.class);
    }


    private Property<String> destDir;

    public Property<String> getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir.set(destDir);
    }

    @Override
    public String toString() {
        return "S3DownloadExtension{" +
                "destDir='" + destDir + '\'' +
                super.toString() +
                '}';
    }
}
