$env:JAVA_HOME="C:\Program Files\Java\jdk1.8.0_202"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

Write-Host "========================================"
Write-Host "正在运行 AKShare QuickStart"
Write-Host "========================================"
Write-Host ""

$userHome = $env:USERPROFILE
$mavenRepo = "$userHome\.m2\repository"

$cp = @()
$cp += "examples\target\classes"
$cp += "akshare-core\target\classes"
$cp += "akshare-common\target\classes"
$cp += "akshare-stock\target\classes"
$cp += "akshare-fund\target\classes"
$cp += "akshare-futures\target\classes"
$cp += "akshare-macro\target\classes"
$cp += "akshare-bond\target\classes"

$jars = @(
    "$mavenRepo\com\squareup\okhttp3\okhttp\3.12.13\okhttp-3.12.13.jar",
    "$mavenRepo\com\squareup\okio\okio\1.15.0\okio-1.15.0.jar",
    "$mavenRepo\org\jsoup\jsoup\1.17.2\jsoup-1.17.2.jar",
    "$mavenRepo\com\fasterxml\jackson\core\jackson-databind\2.16.0\jackson-databind-2.16.0.jar",
    "$mavenRepo\com\fasterxml\jackson\core\jackson-core\2.16.0\jackson-core-2.16.0.jar",
    "$mavenRepo\com\fasterxml\jackson\core\jackson-annotations\2.16.0\jackson-annotations-2.16.0.jar",
    "$mavenRepo\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.16.0\jackson-datatype-jsr310-2.16.0.jar",
    "$mavenRepo\com\github\ben-manes\caffeine\caffeine\2.9.3\caffeine-2.9.3.jar",
    "$mavenRepo\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar",
    "$mavenRepo\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar",
    "$mavenRepo\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar",
    "$mavenRepo\org\projectlombok\lombok\1.18.30\lombok-1.18.30.jar"
)

foreach ($jar in $jars) {
    if (Test-Path $jar) {
        $cp += $jar
    }
}

$classpath = $cp -join ';'

& java -cp $classpath QuickStart

Write-Host ""
Write-Host "========================================"
Write-Host "运行完成"
Write-Host "========================================"
