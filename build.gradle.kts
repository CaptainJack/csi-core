plugins {
	kotlin("multiplatform") version "1.3.41" apply false
	id("nebula.release") version "10.1.2"
//	id("ru.capjack.bintray") version "0.18.1"
}



group = "ru.capjack.tool"

//capjackBintray {
//	publications(
//		":tool-csi-common",
//		":tool-csi-client",
//		":tool-csi-server-jvm"
//	)
//}

subprojects {
	repositories {
		jcenter()
		mavenLocal()
	}
}
