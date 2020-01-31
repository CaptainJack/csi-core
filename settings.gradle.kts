include(
	"common",
	"client",
	"server"
//	"test"
)

rootProject.name = "csi-core"
rootProject.children.forEach { it.name = "${rootProject.name}-${it.name}" }

pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
	}
}