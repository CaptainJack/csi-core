plugins {
	kotlin("multiplatform") version "1.3.41" apply false
	id("nebula.release") version "10.1.2"
	id("ru.capjack.bintray") version "0.19.0"
}

group = "ru.capjack.tool"

subprojects {
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
}
