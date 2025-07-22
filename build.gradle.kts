plugins {
    java
    application
}

group = "ru.SocialMoods"
version = "2.1.0"
description = "NeoProtect V2"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.lanink.cn/repository/maven-public/")
}

dependencies {
    compileOnly("cn.nukkit:Nukkit:MOT-SNAPSHOT")
    
    implementation("com.github.MEFRREEX:FormConstructor:3.1.0")
    
    implementation("org.telegram:telegrambots:6.9.7.1")
    
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}

application {
    mainClass.set("ru.SocialMoods.NeoProtect")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ru.SocialMoods.NeoProtect"
    }
    
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    val props = mapOf("version" to version)
    
    inputs.properties(props)
    
    filesMatching("plugin.yml") {
        expand(props)
    }
} 