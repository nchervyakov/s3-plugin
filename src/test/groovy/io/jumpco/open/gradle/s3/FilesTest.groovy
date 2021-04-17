package io.jumpco.open.gradle.s3

import groovy.json.StringEscapeUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

class FilesTest {
    @Rule
    public TemporaryFolder testProjectDir = new TemporaryFolder()

    @Test
    public void testFiles() {
        Set<String> existing = new HashSet<>();
        File dir = new File("src/test/resources/f1");
        Set<File> files = Files.walk(dir.toPath(), Integer.MAX_VALUE).filter { file -> !Files.isDirectory(file) }
                .map { it.toFile() }
                .collect(Collectors.toSet());
        System.out.println("Files:" + files);
        assert files.size() == 3
        File f11 = new File("src/test/resources/f1/f11.txt")
        assert files.contains(f11)
        File localTest = makeFile(dir, f11, "prod")
        File f11Prod = new File("prod", "f11.txt");
        assert f11Prod == localTest
        File portionTest = new File("f11.txt");
        File f11Portion = makeFile(dir, f11, null)
        assert portionTest == f11Portion
    }

    private File makeFile(File file, File sourceFile, String keyPrefix) {
        if (sourceFile.getPath().startsWith(file.getPath())) {
            def portion = sourceFile.getPath().substring(file.getPath().length())
            if(portion.startsWith(File.separator)) {
                portion = portion.substring(1)
            }
            return keyPrefix != null && keyPrefix.length() > 0 ? new File(keyPrefix, portion) : new File(portion);
        }
        return sourceFile;
    }
}
