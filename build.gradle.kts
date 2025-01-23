plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.kotlin)
	alias(libs.plugins.modPublish)
}

repositories {
	mavenCentral()
	maven("https://maven.isxander.dev/releases") {
		name = "Xander Maven"
	}
	maven("https://maven.terraformersmc.com/releases") {
		name = "Terraformers"
	}
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
		name = "DevAuth"
	}
}

val modName = property("mod_name") as String
val modId = property("mod_id") as String
group = property("maven_group") as String
version = "${libs.versions.modVersion.get()}+${libs.versions.minecraft.get()}"

dependencies {
	// To change the versions, see the gradle.properties file
	minecraft(libs.minecraft)
	mappings("net.fabricmc:yarn:${libs.versions.yarnMappings.get()}:v2")
	modImplementation(libs.fabricLoader)

	modImplementation(libs.fabricApi)
	modImplementation(libs.fabricLanguageKotlin)
	modImplementation(libs.yacl)
	modImplementation(libs.modMenu)
	modRuntimeOnly(libs.devauth)
	compileOnly(libs.mcdevannotations)

	modImplementation(include("net.kyori:adventure-platform-fabric:6.2.0")!!)
	modCompileOnly(libs.adventure)
	implementation(libs.datafaker)
	include(libs.datafaker)
}

tasks {
	processResources {
		val props = mapOf(
			"name" to modName,
			"mod_id" to modId,
			"version" to version,
			"loader_version" to libs.versions.fabricLoader.get(),
			"fabric_kotlin_version" to libs.versions.fabricLanguageKotlin.get(),
			"modmenu_version" to libs.versions.modMenu.get(),
			"yacl_version" to libs.versions.yacl.get()
		)
		inputs.properties(props)
		filesMatching("fabric.mod.json") {
			expand(props)
		}
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesName.get()}" }
		}
	}
	compileKotlin {
		// Yes, I'm using deprecated features, and you can't stop me.
		compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
	}
}

kotlin {
	jvmToolchain(21)
}

publishMods {
	file = tasks.remapJar.get().archiveFile
	modLoaders.add("fabric")
	type = ALPHA
	displayName = "$modName ${libs.versions.modVersion.get()} for Minecraft ${libs.versions.minecraft.get()}"
	changelog = """
		This is a big revamp of the mod. It adds a lot of new features, regarding the use of multiple chat boxes and chat transformers.
		Chat transformers modify the chat message in some way before it is displayed, and they can be chained. There are only filters and replacers for now.
		This is by no means complete, it's in a very early stage, but it has reached a stage where it can be used if you know how to deal with it.
		Please report any issues you find, and I'll try to fix them as soon as possible.
	""".trimIndent()
	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "69pdUAIH"
		minecraftVersions.addAll("1.21.4")
		requires("fabric-api")
		requires("fabric-language-kotlin")
		requires("yacl")
		optional("modmenu")
		featured = true
	}
}
