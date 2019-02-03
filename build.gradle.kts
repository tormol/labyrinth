plugins {
    `java`
    application
    id("com.zyxist.chainsaw") version "0.3.1"
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

application {
    mainClassName = "no.torbmol.labyrinth.Labyrinth"
}
