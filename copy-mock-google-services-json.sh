#!/bin/bash

# https://github.com/firebase/quickstart-android/blob/master/copy_mock_google_services_json.sh

# Exit on error
set -e

# Copy mock google-services file
echo "Using mock google-services.json"

cp mock-google-services.json app/google-services.json
