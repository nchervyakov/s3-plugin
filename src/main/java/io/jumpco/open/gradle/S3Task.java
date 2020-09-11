package io.jumpco.open.gradle;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;

public abstract class S3Task extends DefaultTask {

    @Optional
    @Input
    protected String bucket;

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

    @Internal
    AmazonS3 getS3Client() {

        ProfileCredentialsProvider profileCreds = null;
        String profile = getExt().getProfile();
        if (profile != null) {
            getProject().getLogger().quiet("Using AWS credentials profile: " + profile);
            profileCreds = new ProfileCredentialsProvider(profile);
        } else {
            profileCreds = new ProfileCredentialsProvider();
        }
        AWSCredentialsProviderChain creds = new AWSCredentialsProviderChain(
                new EnvironmentVariableCredentialsProvider(),
                new SystemPropertiesCredentialsProvider(),
                profileCreds,
                new EC2ContainerCredentialsProviderWrapper()
        );

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(creds);

        String region = getExt().getRegion();
        if (region != null) {
            builder.withRegion(region);
        }
        return builder.build();
    }
}