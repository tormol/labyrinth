plugins {
    `java`
    application
    // removes most of the boilerplate from using module
    id("com.dua3.gradle.jpms") version "0.5.1" // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

application {
    mainClassName = "no.torbmol.labyrinth.Labyrinth"
}

// val jlink by tasks.existing(JlinkTask::class) {
// jlink {
//     module = "no.torbmol.labyrinth"
//     main = "no.torbmol.labyrinth.Labyrinth"
//     application = "labyrinth"
//     compress = 2
// }
