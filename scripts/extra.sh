#!/usr/bin/env

port=1234
app_commands=$port

#debug=true

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

  nohup "$@" >> ~/.fsbt/log 2> ~/.fsbt/error.log &

}

# Actually runs the script.
run() {
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




  if [[ -z "$(ps aux | grep -e '[c]om.martiansoftware.nailgun.NGServer 1234')" ]]; then
    execRunner "$java_cmd" \
    ${java_opts[@]} \
    "${java_args[@]}" \
    -cp "$(fix_classpath "$app_classpath")" \
    "${mainclass[@]}" \
    "${app_commands[@]}" \



    counter=0
    isRunning=""
    while [[ -z $isRunning ]] && [ $counter -lt 100 ]
    do
        isRunning="$(lsof -Pi :1234 -sTCP:LISTEN -t)"
        ((counter++))
    done

    if [ $isRunning -eq $! ];
    then
    echo "fsbt server started at port 1234"
        ng-nailgun --nailgun-port $port core.Fsbt "${residual_args[@]}"
    else
        echo "Failed to start fsbt"
    fi
  else
    ng-nailgun --nailgun-port $port core.Fsbt "${residual_args[@]}"
  fi

}