rootProject.name = "Labyrinth"
include("utils", "engine", "Labyrinth")
project(":utils").projectDir = file("Utils")
project(":engine").projectDir = file("Engine")
