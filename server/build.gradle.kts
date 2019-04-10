plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm {
		compilations.all { kotlinOptions.jvmTarget = "1.8" }
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
			implementation(kotlin("reflect"))
		}
		get("jvmTest").dependencies {
			implementation(kotlin("test-junit"))
			implementation("ch.qos.logback:logback-classic:1.2.3")
		}
	}
}