package core

import core.config.ConfigEntry
import core.dependencies.MavenDependency

/**
	* Created by lukmy on 19.03.2017.
	*/
object DSLTest {

	def main(args: Array[String]): Unit = {

		val config = new ConfigBuilder("testProject/build.fsbt")
		val deps = config.config(ConfigEntry.dependencyList).asInstanceOf[List[Dependency]]
		for (dep1 <- deps){
			val d = MavenDependency(dep1.group, dep1.artifact, dep1.version)
			println(d.baseUri)
			d.downloadDependencies
		}

	}
}
