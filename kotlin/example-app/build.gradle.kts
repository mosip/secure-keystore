buildscript {
    val kotlin_version by extra("1.8.0")
    val agp_version by extra("8.0.0")

    repositories {
        mavenLocal()
        google()
        //maven{setUrl("https://oss.sonatype.org/content/repositories/snapshots/")}
        mavenCentral()


    }
    dependencies {
        classpath("com.android.tools.build:gradle:$agp_version")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

