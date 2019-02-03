plugins {
    `java`
    application
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

application {
    mainClassName = "no.torbmol.labyrinth.Labyrinth"
}
