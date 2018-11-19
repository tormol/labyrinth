plugins {
    `java-library`
    // to not have to open a html file to view test results
    // from https://github.com/radarsh/gradle-test-logger-plugin
    id("com.adarshr.test-logger") version "1.5.0"
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
    // the successor of findbugs, but doesn't support java 11 either
    // runs as part of gw check
    //id("com.github.spotbugs") version "1.6.5"
}

repositories {
    jcenter() // aka https://jcenter.bintray.com
    // use https://search.maven.org to find versions
}

dependencies {
    testImplementation("junit:junit:4.12") // latest as of 2018-11-01, junit5 is a different package
    // junit5 info: https://github.com/junit-team/junit5-samples/blob/r5.3.1/junit5-jupiter-starter-gradle-kotlin/build.gradle.kts
    testImplementation("com.google.guava:guava-testlib:19.0") // what's in guava/
    //testImplementation("com.google.guava:guava-testlib:27.0-jre") // latest as of 2018-11-01
}

val jar by tasks.existing(Jar::class) {
    archiveName = "tbm-utils.jar"
}
