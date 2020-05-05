package net.sunshow.code.generator.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

/**
 * author: sunshow.
 */
public class GenerateUtilsTest {

    @Test
    public void testPackageNameToPath() throws Exception {
        Path directory = GenerateUtils.packageNameToPath(new File("/Users/sunshow/Downloads/src").toPath(), "com.example.test");
        System.out.println(directory.toString());
        System.out.println(directory.toAbsolutePath().toString());
    }
}
