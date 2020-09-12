package io.jumpco.open.gradle.s3;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public class SS3Util {
    private final Project project;
    private final String bucket;
    protected String awsAccessKeyId;
    protected String awsSecretAccessKey;

    public SS3Util(Project project, String bucket) {
        this.project = project;
        this.bucket = bucket;
    }

    public SS3Util(Project project, String bucket, String awsAccessKeyId, String awsSecretAccessKey) {
        this.project = project;
        this.bucket = bucket;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    S3Extension getExt() {
        return project.getExtensions().findByType(S3Extension.class);
    }

    public String getBucket() {
        if (bucket == null) {
            return getExt().getBucket();
        }
        return bucket;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    private static class SimpleWSCredentialsProvider implements AWSCredentialsProvider {
        private final AWSCredentials credentials;

        public SimpleWSCredentialsProvider(AWSCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public AWSCredentials getCredentials() {
            return credentials;
        }

        @Override
        public void refresh() {
        }
    }

    public AmazonS3 getS3Client() {
        List<AWSCredentialsProvider> credentialsProviders = new ArrayList<>();
        if (awsAccessKeyId != null) {
            if (awsSecretAccessKey == null) {
                throw new GradleException("Expected awsSecretAccessKey when awsAccessKeyId was provided");
            }
            project.getLogger().quiet("s3:awsAccessKeyId:" + awsAccessKeyId);
            credentialsProviders.add(new SimpleWSCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)));
        }
        credentialsProviders.add(new EnvironmentVariableCredentialsProvider());
        credentialsProviders.add(new SystemPropertiesCredentialsProvider());
        String profile = getExt().getProfile();
        if (profile != null) {
            project.getLogger().quiet("s3:profile: " + profile);
            credentialsProviders.add(new ProfileCredentialsProvider(profile));
        }
        credentialsProviders.add(new EC2ContainerCredentialsProviderWrapper());
        AWSCredentialsProviderChain credentialsProviderChain = new AWSCredentialsProviderChain(
                credentialsProviders.toArray(new AWSCredentialsProvider[0])
        );

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(credentialsProviderChain);

        String region = getExt().getRegion();
        if (region != null) {
            builder.withRegion(region);
        }
        return builder.build();
    }
}