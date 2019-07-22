include(
	"common",
	"client",
	"server"
)

rootProject.name = "tool-csi"
rootProject.children.forEach { it.name = "tool-csi-${it.name}" }

enableFeaturePreview("GRADLE_METADATA")
