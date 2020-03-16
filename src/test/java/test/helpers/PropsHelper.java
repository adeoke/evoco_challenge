package test.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class PropsHelper {
    public Properties propertyFile(String file) {
        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String testProperties = rootPath + file;
        Properties appProps = new Properties();
        try {
            appProps.load(new FileInputStream(testProperties));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return appProps;
    }
}
