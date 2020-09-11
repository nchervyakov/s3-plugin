package io.jumpco.open.gradle.s3


import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class S3PluginTest {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    File packetReader

    @Before
    public void setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << "plugins { id 'io.jumpco.open.gradle.s3' } "

        copy(new File('src/test/resources'), buildFile.parentFile)
    }

    @Test
    public void testUpload() {
        buildFile << """
            io.jumpco.open.gradle.s3.S3BaseConfig.setTesting(true)
            s3 {
                bucket = 'some-bucket'
            }
            s3Downloads {
                docs {
                    destDir = 'sample'
                    keyPrefix = '/'
                } 
            }
            s3Uploads {
                site {
                    bucket = 'some-other-bucket'
                    sourceDir = 'site'
                    keyPrefix = '/'
                }
            }            
        """
        S3BaseConfig.setTesting(true)
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('--stacktrace', '-i', 'docsDownloadTask', 'siteUploadTask')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        println("Temp folder=$buildFile.parent")
        copy(buildFile.parentFile, new File('output'))
        assert result.tasks(TaskOutcome.FAILED).isEmpty()
    }

    def copy(File source, File target) {
        if (source.isDirectory()) {
            source.eachFile {
                copy(it, new File(target, it.name))
            }
        } else {
            if (!target.parentFile.exists()) {
                target.parentFile.mkdirs()
            }
            println("Copy $source.path -> $target.path")
            target.bytes = source.bytes
        }
    }
}
