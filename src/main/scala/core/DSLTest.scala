package core

import java.io.FileReader


/**
	* Created by lukmy on 19.03.2017.
	*/
object DSLTest {


	def main(args: Array[String]): Unit = {
		val reader = new FileReader("C:\\Dev\\fsbt\\testProject\\build.fsbt")
		val dsl = new ConfigDSL
		val res = dsl.parseAll(dsl.configuration, reader)
//		match {
//			case dsl.Success(result, _) => println(result)
//			case dsl.Failure(msg, _) => println(msg)
//		}
		println(res)
	}

}
