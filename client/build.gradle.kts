plugins {
	kotlin("multiplatform")
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
			implementation("ru.capjack.tool:tool-lang:0.4.3-dev.0.uncommitted+b3f4c74")
			implementation("ru.capjack.tool:tool-logging:0.14.0")
			
			api(project(":tool-csi-common"))
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