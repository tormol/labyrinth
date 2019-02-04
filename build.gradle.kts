plugins {
    `java`
    application
    // creates jar with dependencies included
    id("com.github.johnrengelman.shadow") version "4.0.2"
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

application {
    mainClassName = "no.torbmol.labyrinth.Labyrinth"
}

// Don't invoke this task directly; use shadowjar to get a standalone jar
val jar by tasks.existing(Jar::class) {
    manifest {
        attributes(mapOf(
            "Main-Class" to application.mainClassName
        ))
    }
}

val shadowJar by tasks.existing(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveName = "labyrinth.jar"
}
