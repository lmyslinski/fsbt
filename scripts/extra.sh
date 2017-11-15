#!/usr/bin/env bash -x

port=1234

app_commands=$port

execRunner () {
  # print the arguments one to a line, quoting any containing spaces
  [[ $verbose || $debug ]] && echo "# Executing command line:" && {
    for arg; do
      if printf "%s\n" "$arg" | grep -q ' '; then
        printf "\"%s\"\n" "$arg"
      else
        printf "%s\n" "$arg"
      fi
    done
    echo ""
  }

  # we use "exec" here for our pids to be accurate.
  nohup "$@" &
}

# Actually runs the script.
run() {
  # TODO - check for sane environment

  # process the combined args, then reset "$@" to the residuals
  process_args "$@"
  set -- "${residual_args[@]}"
  argumentCount=$#

  #check for jline terminal fixes on cygwin
  if is_cygwin; then
    stty -icanon min 1 -echo > /dev/null 2>&1
    addJava "-Djline.terminal=jline.UnixTerminal"
    addJava "-Dsbt.cygwin=true"
  fi

  # check java version
  if [[ ! $no_version_check ]]; then
    java_version_check
  fi

  if [ -n "$custom_mainclass" ]; then
    mainclass=("$custom_mainclass")
  else
    mainclass=("${app_mainclass[@]}")
  fi

  # Now we check to see if there are any java opts on the environment. These get listed first, with the script able to override them.
  if [[ "$JAVA_OPTS" != "" ]]; then
    java_opts="${JAVA_OPTS}"
  fi

  #custom code

  arg="$mainclass $port"
  echo $arg


  val="ps aux | pgrep -f '$arg'"

  echo $val

  if [[ -z $val ]]; then
    echo "Fsbt is not running, starting the daemon"
    execRunner "$java_cmd" \
    ${java_opts[@]} \
    "${java_args[@]}" \
    -cp "$(fix_classpath "$app_classpath")" \
    "${mainclass[@]}" \
    "${app_commands[@]}" \
    "${residual_args[@]}"
  else
    echo "running"
    ng-nailgun --nailgun-port $port core.Fsbt "$@"
  fi

}