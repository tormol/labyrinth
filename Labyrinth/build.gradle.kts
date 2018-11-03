plugins {
    `java`
    application
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
    compile(project(":engine"))
}

sourceSets {
    getByName("main").java.srcDirs("src")
    getByName("main").resources.srcDirs("res")
    getByName("main").resources.srcDirs("maps")
}

application {
    mainClassName = "labyrinth.Labyrinth"
}
