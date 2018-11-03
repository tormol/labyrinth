plugins {
    `java-library`
    // to not have to open a html file to view test results
    // from https://github.com/radarsh/gradle-test-logger-plugin
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

sourceSets {
    getByName("main").java.srcDirs("src")
}
