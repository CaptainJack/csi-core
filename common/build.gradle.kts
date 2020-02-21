plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
	id("ru.capjack.logging")
}

kotlin {
	jvm()
	js()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.tool:tool-logging")
			implementation("ru.capjack.tool:tool-lang")
			api("ru.capjack.tool:tool-io")
			api("ru.capjack.tool:tool-utils")
		}
		get("commonTest").dependencies {
			implementation(kotlin("test-common"))
			implementation(kotlin("test-annotations-common"))
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		get("jvmTest").dependencies {
			implementation(kotlin("test-junit"))
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
		get("jsTest").dependencies {
			implementation(kotlin("test-js"))
		}
	}
}
