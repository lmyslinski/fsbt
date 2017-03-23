package core

import java.io.FileReader


/**
	* Created by lukmy on 19.03.2017.
	*/
object DSLTest {


	def main(args: Array[String]): Unit = {
		val fileConfig = ConfigDSL.parseConfigFile("C:\\Dev\\fsbt\\testProject\\build.fsbt")
		val defaultConfig = Config.getDefaultConfig()


		if(!fileConfig.keySet.subsetOf(defaultConfig keySet)){
			println("Whooops")
		}

	}

}
