plugins {
    `java-library`
    // to not have to open a html file to view test results
    // from https://github.com/radarsh/gradle-test-logger-plugin
    id("com.adarshr.test-logger") version "1.5.0"
    id("org.gradle.java.experimental-jigsaw") // version "0.1.1"
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

//javaModule.name = "no.torbmol.util"

val jar by tasks.existing(Jar::class) {
    archiveName = "torbmol-utils.jar"
}

// Add examples as a new source type
sourceSets.create("examples") {
    java.srcDirs("src/examples/java/")
    java.srcDirs(sourceSets["main"].java)
}

// Make :check compile the examples
val check by tasks.existing(DefaultTask::class) {
    dependsOn("examplesClasses") // that task is automatically generated!
}

// Create tasks to run the examples
// main = project.property("example").toString() requires the property to be present even when other
// tasks are being run.
// for only two examples this is OK.
// TODO lazy creation
tasks.create("cat", JavaExec::class) {
    main = "cat"
    group = "runExamples"
    classpath = sourceSets["examples"].runtimeClasspath
}
tasks.create("primitivePreProcessor", JavaExec::class) {
    main = "primitivePreProcessor"
    group = "runExamples"
    classpath = sourceSets["examples"].runtimeClasspath
}
