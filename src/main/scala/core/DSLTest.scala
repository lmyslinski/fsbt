package core

import java.io.FileReader


/**
	* Created by lukmy on 19.03.2017.
	*/
object DSLTest {

	val input =
		"""
			|	scalaVersion := "2.12.0"
			| name := "fsbt"
		""".stripMargin

	val input2 = "name := \"fsbt\""

	def main(args: Array[String]): Unit = {
		val reader = new FileReader("C:\\Dev\\fsbt\\testProject\\build.fsbt")
		val dsl = new ConfigDSL
		val res = dsl.parseAll(dsl.configuration, input)
//		match {
//			case dsl.Success(result, _) => println(result)
//			case dsl.Failure(msg, _) => println(msg)
//		}
		println(res)
	}

}
