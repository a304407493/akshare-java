import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class StartQuickStart {
    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        String mavenRepo = userHome + File.separator + ".m2" + File.separator + "repository";
        
        List<URL> classpathUrls = new ArrayList<>();
        
        classpathUrls.add(new File("examples/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-core/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-common/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-stock/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-fund/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-futures/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-macro/target/classes").toURI().toURL());
        classpathUrls.add(new File("akshare-bond/target/classes").toURI().toURL());
        
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "squareup" + File.separator + "okhttp3" + File.separator + "okhttp" + File.separator + "3.12.13" + File.separator + "okhttp-3.12.13.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "squareup" + File.separator + "okio" + File.separator + "okio" + File.separator + "1.15.0" + File.separator + "okio-1.15.0.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "org" + File.separator + "jsoup" + File.separator + "jsoup" + File.separator + "1.17.2" + File.separator + "jsoup-1.17.2.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "fasterxml" + File.separator + "jackson" + File.separator + "core" + File.separator + "jackson-databind" + File.separator + "2.16.0" + File.separator + "jackson-databind-2.16.0.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "fasterxml" + File.separator + "jackson" + File.separator + "core" + File.separator + "jackson-core" + File.separator + "2.16.0" + File.separator + "jackson-core-2.16.0.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "fasterxml" + File.separator + "jackson" + File.separator + "core" + File.separator + "jackson-annotations" + File.separator + "2.16.0" + File.separator + "jackson-annotations-2.16.0.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "fasterxml" + File.separator + "jackson" + File.separator + "datatype" + File.separator + "jackson-datatype-jsr310" + File.separator + "2.16.0" + File.separator + "jackson-datatype-jsr310-2.16.0.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "com" + File.separator + "github" + File.separator + "ben-manes" + File.separator + "caffeine" + File.separator + "caffeine" + File.separator + "2.9.3" + File.separator + "caffeine-2.9.3.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "org" + File.separator + "slf4j" + File.separator + "slf4j-api" + File.separator + "1.7.36" + File.separator + "slf4j-api-1.7.36.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "ch" + File.separator + "qos" + File.separator + "logback" + File.separator + "logback-classic" + File.separator + "1.2.12" + File.separator + "logback-classic-1.2.12.jar");
        addJar(classpathUrls, mavenRepo + File.separator + "ch" + File.separator + "qos" + File.separator + "logback" + File.separator + "logback-core" + File.separator + "1.2.12" + File.separator + "logback-core-1.2.12.jar");
        
        URLClassLoader classLoader = new URLClassLoader(classpathUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
        
        Class<?> quickStartClass = classLoader.loadClass("QuickStart");
        quickStartClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
    }
    
    private static void addJar(List<URL> urls, String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            urls.add(file.toURI().toURL());
        }
    }
}
