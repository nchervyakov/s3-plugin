package io.jumpco.open.gradle.s3;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class S3BaseConfig {
    private static boolean testing = false;

    public static boolean isTesting() {
        return testing;
    }

    public static void setTesting(boolean t) {
        testing = t;
    }

    private Property<String> bucket;

    protected final String name;

    private Property<String> key;


    private Property<String> file;

    private Property<String> keyPrefix;

    public Property<String> getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket.set(bucket);
    }

    public Property<String> getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public Property<String> getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file.set(file);
    }

    public Property<String> getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix.set(keyPrefix);
    }

    public S3BaseConfig(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.bucket = objectFactory.property(String.class);
        this.file = objectFactory.property(String.class);
        this.key = objectFactory.property(String.class);
        this.keyPrefix = objectFactory.property(String.class);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        S3BaseConfig that = (S3BaseConfig) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "S3BaseExtension{" +
                "bucket='" + bucket + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", file='" + file + '\'' +
                ", keyPrefix='" + keyPrefix + '\'' +
                '}';
    }
}
