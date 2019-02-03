plugins {
    `java`
    application
    id("org.gradle.java.experimental-jigsaw") version "0.1.1"
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

//javaModule.name = "no.torbmol.labyrinth"

application {
    mainClassName = "no.torbmol.labyrinth.Labyrinth"
}
