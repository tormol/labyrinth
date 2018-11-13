rootProject.name = "Labyrinth"
include("utils", "engine", "Eat", "Labyrinth")
project(":utils").projectDir = file("Utils")
project(":engine").projectDir = file("Engine")
