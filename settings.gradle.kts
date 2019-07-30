include(
	"common",
	"client",
	"server"
)

rootProject.name = "tool-csi-core"
rootProject.children.forEach { it.name = "${rootProject.name}-${it.name}" }

enableFeaturePreview("GRADLE_METADATA")
