package core

/**
	* Created by lukmy on 19.03.2017.
	*/

import scala.util.parsing.combinator._


class ConfigDSL extends JavaTokenParsers{


	def stringAssignment = ident~":="~stringLiteral ^^ { case (a~b~c) => (a, stripQuotes(c)) }

	def expression = stringAssignment

	def configuration = expression | rep(expression)

	def stripQuotes(input: String) = input.substring(1, input.length()-1)

}
