package core.dependencies

/**
  * Created by humblehound on 21.07.17.
  */
object MavenDependencyScope extends Enumeration {
  val Provided = Value("provided")
  val Compile = Value("compile")
  val Runtime = Value("runtime")
  val Test = Value("test")
}
