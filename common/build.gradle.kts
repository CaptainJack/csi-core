plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}

kotlin {
	jvm {
		compilations.all { kotlinOptions.jvmTarget = "1.8" }
	}
	js {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.tool:tool-lang:0.5.0")
			
			api("ru.capjack.tool:tool-io:0.2.0")
			api("ru.capjack.tool:tool-utils:0.1.1")
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