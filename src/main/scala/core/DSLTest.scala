package core

import core.config.ConfigEntry

/**
	* Created by lukmy on 19.03.2017.
	*/
object DSLTest {

	def main(args: Array[String]): Unit = {
		val config = new ConfigBuilder("C:\\Dev\\fsbt\\testProject\\build.fsbt")
		print(config.config(ConfigEntry.dependencyList))
	}
}
