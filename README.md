# Hikvision Download Assistant

A command-line tool to make searching and downloading photos and videos from your Hikvision cameras easy... 
without requiring installation of any Hikvision software on your computer!

## Usage

Hikvision Download Assistant is simple command line tool that connects to your Hikvision camera or NVR
using the ISAPI web API to perform searches for videos and photos. It writes `curl` commands to the screen
that you can then use to download each photo or video from the ISAPI API.

Like most command-line tools, it is designed to be easily composed with other command-line tools.
The list of search results goes to `stdout`.
All other helpful header and footer text is sent to `stderr` so it can be easily excluded from pipes and redirects.

Run using `java -jar hikvision-download-assistant.jar <options>`.

### Usage statement

```
Usage: java -jar hikvision-download-assistant.jar [-hqV] [-d=<tableDelimiter>] [-f=<fromTime>] [-o=<outputFormat>] [-p=<outputPassword>] [-t=<toTime>] [-u=<outputUsername>] HOST USERNAME PASSWORD
      HOST                 Connect to this host or IP address to perform search.
      USERNAME             Use this username when connecting to perform search.
      PASSWORD             Use this password when connecting to perform search.
  -d, --table-delimiter=<tableDelimiter>
                           The column delimiter for table output. Defaults to '|'.
  -f, --from-time=<fromTime>
                           Search starting from this time, entered using English natural language. Defaults to '24 hours ago'.
  -h, --help               Show this help message and exit.
  -o, --output=<outputFormat>
                           Output format. Can be 'table' or 'json'. Defaults to 'table'.
  -p, --output-password=<outputPassword>
                           Output a different password in the printed curl commands, e.g. '$PASSWORD'.
  -q, --quiet              Suppress header and footer.
  -t, --to-time=<toTime>   Search up to this time, entered using English natural language. Defaults to 'now'.
  -u, --output-username=<outputUsername>
                           Output a different username in the printed curl commands, e.g. '$USERNAME'.
  -V, --version            Print version information and exit.
```

### Examples

With default options:

```
$ java -jar hikvision-download-assistant.jar 192.168.1.64 admin passsword123
Getting photos and videos from "Saturday May 30, 2020 at 7:43:52 PM PDT" to "Sunday May 31, 2020 at 7:43:53 PM PDT"

Type|EventType|Start|End|Curl
-----------------------------
VIDEO|ALLEVENT|2020-05-31T19:01:06-0700|2020-05-31T19:01:22-0700|curl -f --anyauth --user admin:password123 -X GET -d '<downloadRequest><playbackURI>rtsp://192.168.1.64/Streaming/tracks/101/?starttime=20200601T020106Z&amp;endtime=20200601T020122Z&amp;name=ch01_00000000008001213&amp;size=6836476</playbackURI></downloadRequest>' 'http://192.168.1.64/ISAPI/ContentMgmt/download' --output 2020-05-31T19-01-06.mp4
PHOTO|MOTION|2020-05-31T19:01:09-0700|2020-05-31T19:01:09-0700|curl -f --anyauth --user admin:password123 'http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200601T020109Z&endtime=20200601T020109Z&name=ch01_00000000005030201&size=574906' --output 2020-05-31T19-01-09.jpeg
PHOTO|MOTION|2020-05-31T19:01:10-0700|2020-05-31T19:01:10-0700|curl -f --anyauth --user admin:password123 'http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200601T020110Z&endtime=20200601T020110Z&name=ch01_00000000005030301&size=710770' --output 2020-05-31T19-01-10.jpeg
VIDEO|ALLEVENT|2020-05-31T19:35:29-0700|2020-05-31T19:35:47-0700|curl -f --anyauth --user admin:password123 -X GET -d '<downloadRequest><playbackURI>rtsp://192.168.1.64/Streaming/tracks/101/?starttime=20200601T023529Z&amp;endtime=20200601T023547Z&amp;name=ch01_00000000008001313&amp;size=2933900</playbackURI></downloadRequest>' 'http://192.168.1.64/ISAPI/ContentMgmt/download' --output 2020-05-31T19-35-29.mp4
PHOTO|MOTION|2020-05-31T19:35:34-0700|2020-05-31T19:35:34-0700|curl -f --anyauth --user admin:password123 'http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200601T023534Z&endtime=20200601T023534Z&name=ch01_00000000005031401&size=537158' --output 2020-05-31T19-35-34.jpeg

Found 2 videos and 4 photos
```

With `--output json`:

```
$ java -jar hikvision-download-assistant.jar 192.168.1.64 admin passsword123 --output json
Getting photos and videos from "Saturday May 30, 2020 at 7:43:52 PM PDT" to "Sunday May 31, 2020 at 7:43:53 PM PDT"

[ {
  "mediaType" : "VIDEO",
  "startTime" : 1590976866000,
  "endTime" : 1590976882000,
  "eventType" : "ALLEVENT",
  "curlCommand" : "curl -f --anyauth --user admin:password123 -X GET -d '<downloadRequest><playbackURI>rtsp://192.168.1.64/Streaming/tracks/101/?starttime=20200601T020106Z&amp;endtime=20200601T020122Z&amp;name=ch01_00000000008001213&amp;size=6836476</playbackURI></downloadRequest>' 'http://192.168.1.64/ISAPI/ContentMgmt/download' --output 2020-05-31T19-01-06.mp4"
}, {
  "mediaType" : "PHOTO",
  "startTime" : 1590976869000,
  "endTime" : 1590976869000,
  "eventType" : "MOTION",
  "curlCommand" : "curl -f --anyauth --user admin:password123 'http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200601T020109Z&endtime=20200601T020109Z&name=ch01_00000000005030201&size=574906' --output 2020-05-31T19-01-09.jpeg"
}, {
  "mediaType" : "VIDEO",
  "startTime" : 1590978929000,
  "endTime" : 1590978947000,
  "eventType" : "ALLEVENT",
  "curlCommand" : "curl -f --anyauth --user admin:password123 -X GET -d '<downloadRequest><playbackURI>rtsp://192.168.1.64/Streaming/tracks/101/?starttime=20200601T023529Z&amp;endtime=20200601T023547Z&amp;name=ch01_00000000008001313&amp;size=2933900</playbackURI></downloadRequest>' 'http://192.168.1.64/ISAPI/ContentMgmt/download' --output 2020-05-31T19-35-29.mp4"
}, {
  "mediaType" : "PHOTO",
  "startTime" : 1590978934000,
  "endTime" : 1590978934000,
  "eventType" : "MOTION",
  "curlCommand" : "curl -f --anyauth --user admin:password123 'http://192.168.1.64/ISAPI/Streaming/tracks/103/?starttime=20200601T023534Z&endtime=20200601T023534Z&name=ch01_00000000005031401&size=537158' --output 2020-05-31T19-35-34.jpeg"
} ]

Found 2 videos and 2 photos
```

### Unix/MacOS pipeline examples

Filtering and choosing a column using `jq`:

```
java -jar hikvision-download-assistant.jar 192.168.1.64 admin $PASSWORD --quiet --output json | jq '.[] | select(.eventType=="MOTION") | .startTime'
```

Filtering and choosing a column using `grep` and `cut`:

```
java -jar hikvision-download-assistant.jar 192.168.1.64 admin $PASSWORD --quiet | grep MOTION | cut -d '|' -f 3
```

Executing all of the returned curl commands to download all of the results to the current 
working directory (assuming `bash` is your shell):

```
java -jar hikvision-download-assistant.jar 192.168.1.64 admin $PASSWORD --quiet | cut -d '|' -f 5 | while read curl_cmd; do eval $curl_cmd; done
```

### Using `--from-time` and `--to-time`

These options take English natural language and try their best to understand what you mean.

Examples:

- `now`
- `10 pm yesterday`
- `noon yesterday`
- `5 pm` - 5 pm today
- `6:50 today` - caution, this is 6:50 AM, no matter what current time
- `6:50 am today` or `6:50 pm today` - safer alternative so you don't get AM by accident
- `last thursday` - at the current time of day, but different date
- `1 week ago` - at the current time of day, but different date
- `oct 3rd` - at the current time of day, but different date
- `2/14/20 at 2 am`
- ...[and many more](http://natty.joestelmach.com/doc.jsp)

It will not always guess correctly, so the first line of output will always print what it guessed so you can confirm.

Your `--to-time` can be a date/time in the future. This may be helpful if your camera's system time is wrong.

## Why would I need this?

Why, you ask? Several reasons!

- The Hikvision in-browser web UI software for browsing and downloading videos and photos is *no
  longer compatible with MacOS at all*. When you open their web UI, the "Live View" 
  and "Configuration" tabs will appear, but the "Picture" and "Playback" tabs will not appear.
- The Hikvision in-browser web UI software for browsing and downloading videos and photos for
  MS Windows works but still requires installing Hikvision software on your computer.
  Some people may not want to install this software.
- Hikvision offers a free application called
  [iVMS-4200](https://www.hikvision.com/en/products/software/ivms-4200/)
  which does offer these capabilities, which does work on both MacOS and Windows.
  However, some people might wish to avoid installing it.
  Some users have complained in online forums about the design and usability of the application.
  Also, installing this software on a Mac requires entering your admin password several times,
  indicating that it is making some kind of changes to your OS, and it's not clear how to uninstall
  all of its side effects.
- There doesn't seem to be a linux version of iVMS-4200, so there is no obvious way to download
  photos and videos on a linux computer.
- Both the Hikvision web UI and the iVMS-4200 only allow you to download one 
  page of video or photo search results at a time, so it is not clear how to easily download
  large numbers of photos and videos.
- You can still configure your camera using the Hikvision in-browser web UI software, so if
  there were another tool to help you download videos and photos, then you wouldn't need to
  install any software from Hikvision. Well, now there is!

## Prerequisites

### Prerequisites on your computer

This application is written in Java, and therefore can run on pretty much any platform.

You'll need a Java Runtime Environment on your computer. It must be Java version 11 or higher.

Good news, you might already have one! Open your command terminal and type `java -version` to check.

Don't have one? You could install any JRE that you like, for example you could
choose to install [OpenJDK](https://openjdk.java.net/install/index.html), 
e.g. `brew install openjdk` on a Mac.

### Prerequisites on your Hikvision device(s)

Note that this app uses digest authentication, which is the default setting on Hikvision cameras, so you do *not* need
to enable basic authentication to use this app.

You'll want to make sure that your camera's system clock is correct. You can check this in the camera's web UI
under System -> System Settings -> Time Settings.

You may wish to enable the Daylight Savings Time feature to avoid having your computer's
time and your camera's time differ by one hour in the summer. This can be enabled in the camera's web UI
under System -> System Settings -> DST. Be sure to also change the DST start time, end time, and bias
to match your local DST schedule.
For the US you would check the `Enable DST box`, start on `Mar Second Sun 02`, end on `Nov First Sun 02`, 
and set a bias of `60minutes`. Don't forget to click "Save".

### Related tools (not required)

- You might like to install [VLC](https://www.videolan.org/vlc/) to view
  the downloaded videos, e.g. `brew cask install vlc` on a Mac.
- You'll probably want [`curl`](https://ec.haxx.se), but you already have that.

## Compatible cameras and DVR/NVRs

This was developed and tested using my Hikvision model DS-2CD2185FWD-I IP camera
using Firmware version `V5.6.3 build 190923`.

Theoretically, it should work with any Hikvision camera.

I believe that the Hikvision NVRs offer the same ISAPI endpoint (`POST /ISAPI/ContentMgmt/search`) so
it should theoretically work with those too.

You mileage may vary. Github issues and PRs are welcome.

## Building

If you would prefer to compile the source code yourself, you'll need to install a Java JDK, 
e.g. `brew install openjdk` on a Mac.

To build, run `./mvnw clean package` from the top-level directory of the project.

The compiler will output a file called `target/hikvision-download-assistant-1.0-SNAPSHOT-jar-with-dependencies.jar`.
If you'd like, you can copy this to whatever directory and filename you like.

Run it with `java -jar target/hikvision-download-assistant-1.0-SNAPSHOT-jar-with-dependencies.jar <options>`

## Copyright and Licence

`Copyright (c) 2020 Ryan Richard`

Licensed under MIT. See [LICENSE](LICENSE) file for license.

The author of this software is not affiliated with Hikvision Digital Technology Co., the maker of Hikvision cameras, 
and this software is not endorsed by Hikvision Digital Technology Co.
