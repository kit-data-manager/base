#!/bin/bash

# This script checks all examples in the examples folder. The filename states the outcome
# at the beginning of the filename with the key 'valid' or 'invalid'. If none is given, 
# the test will be performed but ignored for the end result.

# Color coding test results.
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

exitstate="success"

# Check all xml files.
for i in $(ls ../examples | grep "\.xml$"); do

  # Get the indentifier 'valid' or 'invalid'. All others are unkown.
  token="$(echo "$i" | awk -F "-" '{print $1}')"
  # Check the xml files and store the return value.
  if [ -z "$1" ]; then
    validator/xsdv.sh ../dataorganization.xsd ../examples/$i > /dev/null 
  else
    validator/xsdv.sh ../dataorganization.xsd ../examples/$i 
  fi
  errorcode=$?

  # Invalid tests are successfull if the result is invalid.
  if [[ "$token" == "invalid" ]]; then
    if [[ "$errorcode" != "0" ]]; then
      errorcode="0"
    else
      errorcode="1"
    fi
  elif [[ "$token" == "valid"  ]]; then
    :
  else
    errorcode="unknown"
  fi

  # Command line output.
  if [[ "$errorcode" == "0" ]]; then
    printf "[${GREEN}SUCCESS${NC}] "$i"\n"
  elif  [[ "$errorcode" == "unknown" ]]; then
    printf "[${YELLOW}UNKOWN${NC} ] "$i"\n"
  else
    printf "[${RED}FAILED ${NC}] "$i"\n"
    exitstate="failed"
  fi

  if [ ! -z "$1" ]; then
    echo "----------------------------------------------------------------------------------------------------------------"
  fi

done

# Exit with an error code if one test failed.
if [[ "$exitstate" == "failed" ]]; then
  exit 1 # At least one test failed
else
  exit 0 # Sunshine everywhere ;-)
fi



