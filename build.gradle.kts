plugins {
    `java`
    application
    // generates .classpath and .project files. Run `gw eclipse`
    `eclipse`
}

dependencies {
    compile(project(":utils"))
}

sourceSets {
    getByName("main").java.srcDirs("src/main")
    getByName("main").resources.srcDirs("res")
    getByName("main").resources.srcDirs("maps")
}

application {
    mainClassName = "labyrinth.Labyrinth"
}
