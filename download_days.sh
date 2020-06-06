#!/usr/bin/env bash

#
# Copyright (c) 2020 Ryan Richard
#
# An example of using hikvision-download-assistant to download
# several days worth of videos and photos, organizing them into
# directories per day, and presenting a simple UI for browsing
# and viewing them.
#
# Note: Requires ffmpeg and jq
#
# Usage: HIKVISION_PASSWORD=mypass download_days <number_of_previous_days_to_download> <download_directory>
# When the argument is 0, downloads only today's photos and videos up to now.
# When the argument is N, downloads today's photos and videos up to now plus the previous N days.
#
# Assumes that you have installed this script into the same directory as the hikvision-download-assisant.jar file.
#
# This script is safe to run multiple times for the same output directory. It will retain your previous files
# and will only download photos and videos that were not previously downloaded.
#

set -eo pipefail

# The user should set these environment variables or else it will use these defaults
: "${HIKVISION_USERNAME:=admin}"
: "${HIKVISION_HOST:=192.168.1.64}"

# Get the command-line arguments
NUM_DAYS=$1
DOWNLOAD_DIR=$2

if [[ -z "$HIKVISION_PASSWORD" ]]; then
  echo "ERROR: Please use \$HIKVISION_PASSWORD to set your password." >&2
  exit 1
fi

if [[ -z "$NUM_DAYS" ]] || ! [[ $NUM_DAYS =~ ^[0-9]+$ ]]; then
  echo "ERROR: Please use number of days to download as the first argument." >&2
  exit 1
fi

if [[ -z "$DOWNLOAD_DIR" ]]; then
  echo "ERROR: Please specify download destination directory as the second argument." >&2
  exit 1
fi

set -u

IFS=$'\n'
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
INDEX_FILENAME="index.html"

mkdir -p "$DOWNLOAD_DIR"

for DAYS_AGO in $(seq 0 "$NUM_DAYS"); do

  if [[ $DAYS_AGO -eq 0 ]]; then
    from="today at 12:00:00 AM"
    to="now"
  else
    from="$DAYS_AGO days ago at 12:00:00 AM"
    to="$DAYS_AGO days ago at 11:59:59 PM"
  fi

  SEARCH_RESULT=$(java -jar "$SCRIPT_DIR/hikvision-download-assistant.jar" \
    "$HIKVISION_HOST" "$HIKVISION_USERNAME" "$HIKVISION_PASSWORD" \
    --from-time "$from" --to-time "$to" \
    --output json --quiet)

  RESULT_DATE=$(echo "$SEARCH_RESULT" | jq -r .metadata.fromTime | cut -c1-10)

  DAY_DIR="$DOWNLOAD_DIR/$RESULT_DATE"
  mkdir -p "$DAY_DIR"
  pushd "$DAY_DIR" >/dev/null

  for RESULT in $(echo "$SEARCH_RESULT" | jq -r --compact-output '.results[]'); do

    CURL_COMMAND=$(echo "$RESULT" | jq -r '.curlCommand')
    DOWNLOAD_FILENAME=$(echo "$CURL_COMMAND" | rev | cut -d ' ' -f 1 | rev)

    if [[ $DOWNLOAD_FILENAME == *mp4 ]]; then

      # For videos, download and transcode
      FIXED_FILENAME="$(dirname "$DOWNLOAD_FILENAME")/$(basename "$DOWNLOAD_FILENAME" .mp4).fixed.mp4"

      if ! [[ -f $FIXED_FILENAME ]]; then
        echo "Downloading $DOWNLOAD_FILENAME"
        eval "$CURL_COMMAND -s"
        echo "Transcoding $DOWNLOAD_FILENAME"
        ffmpeg -err_detect ignore_err -i "$DOWNLOAD_FILENAME" -c copy "$FIXED_FILENAME" -hide_banner -loglevel warning
        rm "$DOWNLOAD_FILENAME"
      else
        echo "Already downloaded $DOWNLOAD_FILENAME"
      fi

    else

      # For photos, just download
      if ! [[ -f $DOWNLOAD_FILENAME ]]; then
        echo "Downloading $DOWNLOAD_FILENAME"
        eval "$CURL_COMMAND -s"
      else
        echo "Already downloaded $DOWNLOAD_FILENAME"
      fi
    fi

  done # done downloading all files in the day directory

  echo "Making $INDEX_FILENAME for directory $DAY_DIR"
  echo '<a href="../'"$INDEX_FILENAME"'">Home</a>' >"$INDEX_FILENAME"
  echo "<h1>Downloaded photos and videos for $RESULT_DATE</h1>" >>"$INDEX_FILENAME"
  FILE_ARRAY=($(find "$(pwd)" -maxdepth 1 \( -name "*.fixed.mp4" -o -name "*.jpeg" \) | sort))
  FILE_COUNT=${#FILE_ARRAY[@]}
  for ((i = 0; i < FILE_COUNT; i++)); do
    CURRENT_FILE=${FILE_ARRAY[$i]}
    CURRENT_FILE_BASENAME=$(basename "$CURRENT_FILE")
    CURRENT_FILE_HTML="${CURRENT_FILE_BASENAME}.html"

    PREVIOUS_FILE=""
    if ((i > 0)); then
      PREVIOUS_FILE=${FILE_ARRAY[$i - 1]}
      PREVIOUS_FILE_BASENAME=$(basename "$PREVIOUS_FILE")
    fi

    NEXT_FILE=""
    if ((i < FILE_COUNT - 1)); then
      NEXT_FILE=${FILE_ARRAY[$i + 1]}
      NEXT_FILE_BASENAME=$(basename "$NEXT_FILE")
    fi

    echo '<a href="'"$INDEX_FILENAME"'">Back to day</a>' >"$CURRENT_FILE_HTML"
    if [[ -n "$PREVIOUS_FILE" ]]; then
      echo ' | <a href="'"$PREVIOUS_FILE_BASENAME"'.html">Previous</a>' >>"$CURRENT_FILE_HTML"
    else
      echo ' | Previous' >>"$CURRENT_FILE_HTML"
    fi
    if [[ -n "$NEXT_FILE" ]]; then
      echo ' | <a href="'"$NEXT_FILE_BASENAME"'.html">Next</a>' >>"$CURRENT_FILE_HTML"
    else
      echo ' | Next' >>"$CURRENT_FILE_HTML"
    fi
    echo '<h1>'"$CURRENT_FILE_BASENAME"'</h1>' >>"$CURRENT_FILE_HTML"

    if [[ "$CURRENT_FILE" == *mp4 ]]; then
      echo '<video autoplay controls width="100%" title="'"$CURRENT_FILE_BASENAME"'" src="'"$CURRENT_FILE_BASENAME"'">Cannot show video</video>' >>"$CURRENT_FILE_HTML"
      echo '<a href="'"$CURRENT_FILE_HTML"'"><video controls width="14%" preload="none" title="'"$CURRENT_FILE_BASENAME"'" src="'"$CURRENT_FILE_BASENAME"'">Cannot show video</video></a>' >>"$INDEX_FILENAME"
    else
      echo '<a href="'"$CURRENT_FILE_BASENAME"'"><img src="'"$CURRENT_FILE_BASENAME"'" width="100%"/></a>' >>"$CURRENT_FILE_HTML"
      echo '<a href="'"$CURRENT_FILE_HTML"'"><img width="14%" src="'"$CURRENT_FILE_BASENAME"'" title="'"$CURRENT_FILE_BASENAME"'"/></a>' >>"$INDEX_FILENAME"
    fi
  done

  if [[ FILE_COUNT -eq 0 ]]; then
    echo "<h1>No downloaded photos or videos for $RESULT_DATE</h1>" >"$INDEX_FILENAME"
  fi

  popd >/dev/null

done

echo "Making $INDEX_FILENAME for directory $DOWNLOAD_DIR"
pushd "$DOWNLOAD_DIR" >/dev/null
echo '<h1>Downloaded photos and videos by day</h1>' >"$INDEX_FILENAME"
echo '<h3>Powered by <a href="https://github.com/cfryanr/hikvision-download-assistant">hikvision-download-assistant</a></h3><ul>' >>"$INDEX_FILENAME"
for DATE_DIR in $(find ./* -maxdepth 1 -type d | sort); do
  DATE_DIR=$(basename "$DATE_DIR")
  echo '<li><a href="'"$DATE_DIR"'/'"$INDEX_FILENAME"'">'"$(basename "$DATE_DIR")"'</a></li>' >>"$INDEX_FILENAME"
done
echo '</ul>' >>"$INDEX_FILENAME"

echo "Done. Wrote top-level index file:$(pwd)/$INDEX_FILENAME"

if [[ $(uname) == "Darwin" ]]; then
  open $INDEX_FILENAME
fi
