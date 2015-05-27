#!/bin/bash
#fail if failed function 

exitOnError(){
  errorCode="$?";
  if [ $errorCode -ne "0" ]; then
    echo "running failed, error code:" $errorCode
    if [[ -n $1 ]]; then
      echo $1
    fi
    exit 1
  fi
}

lastSuccessful(){
  errorCode="$?";
  if [ $errorCode -ne "0" ]; then
    LAST_SUCCESSFUL="0"
    echo "running failed, error code:" $errorCode
    if [[ -n $1 ]]; then
      echo $1
    fi
  else
    LAST_SUCCESSFUL="1"
  fi
  
}

#add extra pass
args=" "
for arg in "$@"
do
   args+="$arg "
done