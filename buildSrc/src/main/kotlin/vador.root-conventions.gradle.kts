import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.LineEnding
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML

plugins {
  java
  idea
  id("com.diffplug.spotless")
  id("io.gitlab.arturbosch.detekt")
  id("com.adarshr.test-logger")
  id("com.github.spotbugs") apply false
}

group = GROUP_ID

version = VERSION

description = "Vador - A framework for POJO/Data Structure/Bean validation"

repositories { mavenCentral() }

spotless {
  lineEndings = LineEnding.PLATFORM_NATIVE
  kotlin {
    ktfmt().googleStyle()
    target("**/*.kt")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**", "**/bin/**", "**/out/**")
  }
  kotlinGradle {
    ktfmt().googleStyle()
    target("**/*.gradle.kts")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**", "**/bin/**", "**/out/**")
  }
  java {
    toggleOffOn()
    target("**/*.java")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**", "**/bin/**", "**/out/**")
  }
  format("xml") {
    targetExclude("pom.xml")
    target("*.xml")
    eclipseWtp(XML)
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  baseline = file("$rootDir/detekt/baseline.xml")
  config.setFrom(file("$rootDir/detekt/config.yml"))
}

testlogger.theme = MOCHA

tasks {
  spotbugsMain.get().enabled = false
  spotbugsTest.get().enabled = false
  spotbugs.ignoreFailures.set(true)
}
