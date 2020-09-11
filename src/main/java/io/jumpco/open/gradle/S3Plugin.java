package io.jumpco.open.gradle;


import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class S3Plugin implements Plugin<Project> {

    public void apply(Project project) {
        project.getExtensions().create("s3", S3Extension.class);
    }
}
