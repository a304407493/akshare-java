@echo off
chcp 65001
@echo off

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202
set PATH=%JAVA_HOME%\bin;%PATH%

echo ========================================
echo 正在运行 AKShare QuickStart
echo ========================================
echo.

cd /d "%~dp0"

set CP=examples\target\classes
set CP=%CP%;akshare-core\target\classes
set CP=%CP%;akshare-common\target\classes
set CP=%CP%;akshare-stock\target\classes
set CP=%CP%;akshare-fund\target\classes
set CP=%CP%;akshare-futures\target\classes
set CP=%CP%;akshare-macro\target\classes
set CP=%CP%;akshare-bond\target\classes

for %%i in ("%USERPROFILE%\.m2\repository\com\squareup\okhttp3\okhttp\3.12.13\okhttp-3.12.13.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\squareup\okio\okio\1.15.0\okio-1.15.0.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\org\jsoup\jsoup\1.17.2\jsoup-1.17.2.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.16.0\jackson-databind-2.16.0.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.16.0\jackson-core-2.16.0.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.16.0\jackson-annotations-2.16.0.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.16.0\jackson-datatype-jsr310-2.16.0.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\com\github\ben-manes\caffeine\caffeine\2.9.3\caffeine-2.9.3.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar") do set CP=%CP%;%%i
for %%i in ("%USERPROFILE%\.m2\repository\org\projectlombok\lombok\1.18.30\lombok-1.18.30.jar") do set CP=%CP%;%%i

java -cp "%CP%" QuickStart

echo.
echo ========================================
echo 运行完成
echo ========================================
pause
