@echo off
chcp 65001
@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo 正在运行 AKShare QuickStart
echo ========================================
echo.

set CLASSPATH=examples\target\classes;akshare-core\target\classes;akshare-common\target\classes;akshare-stock\target\classes;akshare-fund\target\classes;akshare-futures\target\classes;akshare-macro\target\classes;akshare-bond\target\classes
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\squareup\okhttp3\okhttp\3.12.13\okhttp-3.12.13.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\squareup\okio\okio\1.15.0\okio-1.15.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\jsoup\jsoup\1.17.2\jsoup-1.17.2.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.16.0\jackson-databind-2.16.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.16.0\jackson-core-2.16.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.16.0\jackson-annotations-2.16.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.16.0\jackson-datatype-jsr310-2.16.0.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\com\github\ben-manes\caffeine\caffeine\2.9.3\caffeine-2.9.3.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar
set CLASSPATH=%CLASSPATH%;%USERPROFILE%\.m2\repository\org\projectlombok\lombok\1.18.30\lombok-1.18.30.jar

java -cp "%CLASSPATH%" QuickStart

echo.
echo ========================================
echo 运行完成
echo ========================================
pause
