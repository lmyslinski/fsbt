package core.dependencies

object MavenDependencyScope extends Enumeration {
  val Provided = Value("provided")
  val Compile = Value("compile")
  val Runtime = Value("runtime")
  val Import = Value("import")
  val Test = Value("test")
}
