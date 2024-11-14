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
}

kotlin {
	jvmToolchain(21)
}

publishMods {
	file = tasks.remapJar.get().archiveFile
	modLoaders.add("fabric")
	type = STABLE
	displayName = "$modName ${libs.versions.modVersion.get()} for Minecraft ${libs.versions.minecraft.get()}"
	changelog = """
		- Add options to change the chat's focused/unfocused height and width beyond the normal limits
		- Add chat peeking (hold keybind to focus chat)
		- Add the ability keep chat open at all times (chat peeking but permanent)
		- Fix timestamps not being correct when grouping time frame is changed
	""".trimIndent()
	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "69pdUAIH"
		minecraftVersions.addAll("1.21.2", "1.21.3")
		requires("fabric-api")
		requires("fabric-language-kotlin")
		requires("yacl")
		optional("modmenu")
		featured = true
	}
}
