package io.jumpco.open.gradle.s3;

import org.gradle.api.*;


public class S3Plugin implements Plugin<Project> {

    public void apply(Project project) {
        S3Extension extension = project.getExtensions().create("s3", S3Extension.class);
        NamedDomainObjectContainer<S3UploadConfig> uploadContainer = project.container(S3UploadConfig.class, new NamedDomainObjectFactory<S3UploadConfig>() {
            public S3UploadConfig create(String name) {
                return new S3UploadConfig(name, project.getObjects());
            }
        });
        project.getExtensions().add("s3Uploads", uploadContainer);
        NamedDomainObjectContainer<S3DownloadConfig> downloadContainer = project.container(S3DownloadConfig.class, new NamedDomainObjectFactory<S3DownloadConfig>() {
            public S3DownloadConfig create(String name) {
                return new S3DownloadConfig(name, project.getObjects());
            }
        });
        project.getExtensions().add("s3Downloads", downloadContainer);
        uploadContainer.all(new Action<S3UploadConfig>() {
            @Override
            public void execute(S3UploadConfig config) {
                project.getLogger().info("S3Upload:config:" + config);
                project.getTasks().register(config.getName() + "UploadTask", S3Upload.class, new Action<S3Upload>() {
                    @Override
                    public void execute(S3Upload task) {
                        task.setBucket(config.getBucket().getOrElse(getExt(project).getBucket()));
                        task.setSourceDir(config.getSourceDir().getOrNull());
                        task.setOverwrite(config.getOverwrite().getOrElse(false));
                        task.setKeyPrefix(config.getKeyPrefix().getOrNull());
                        task.setFile(config.getFile().getOrNull());
                        project.getLogger().info("S3Upload:registered:" + task.getName() + ":" + config);
                    }
                });
            }
        });
        downloadContainer.all(new Action<S3DownloadConfig>() {
            @Override
            public void execute(S3DownloadConfig config) {
                project.getLogger().info("S3Download:config:" + config);
                project.getTasks().register(config.getName() + "DownloadTask", S3Download.class, new Action<S3Download>() {
                    @Override
                    public void execute(S3Download task) {
                        task.setBucket(config.getBucket().getOrElse(getExt(project).getBucket()));
                        task.setKeyPrefix(config.getKeyPrefix().getOrNull());
                        task.setKey(config.getKey().getOrNull());
                        task.setFile(config.getFile().getOrNull());
                        task.setDestDir(config.getDestDir().getOrNull());
                        project.getLogger().info("S3Download:registered:" + task.getName() + ":" + config);
                    }
                });
            }
        });
    }

    private S3Extension getExt(Project project) {
        S3Extension s3Extension = project.getExtensions().findByType(S3Extension.class);
        if (s3Extension == null) {
            throw new GradleException("Expected s3");
        }
        return s3Extension;
    }
}