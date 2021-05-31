rootProject.name = "csi-core"

include(
	"common",
	"client",
	"server"
)

rootProject.children.forEach { it.name = "${rootProject.name}-${it.name}" }
