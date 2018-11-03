rootProject.name = "Labyrinth"
include("utils", "engine", "Eat", "Labyrinth")
project(":utils").projectDir = file("Utilities/Utils")
project(":engine").projectDir = file("Engine")
