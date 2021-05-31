plugins {
	kotlin("multiplatform")
	id("ru.capjack.depver")
	id("ru.capjack.publisher")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation("ru.capjack.tool:tool-logging")
			implementation("ru.capjack.tool:tool-lang")
			api("ru.capjack.tool:tool-io")
			api("ru.capjack.tool:tool-utils")
		}
		get("commonTest").dependencies {
			implementation(kotlin("test"))
		}
	}
}
