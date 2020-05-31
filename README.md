# Hikvision Download Assistant

A command-line tool to make searching and downloading photos and videos from your Hikvision cameras easy... 
without requiring installation of any Hikvision software on your computer!

## Prerequisites on your computer

This application is written in Java, and therefore can run on pretty much any platform.

You'll need a Java Runtime Environment on your computer.

Good news, you probably already have one! Open your command terminal and type `java -version` to check.

Don't have one? You could install any JRE that you like, for example you could
choose to install [OpenJDK](https://openjdk.java.net/install/index.html), 
e.g. `brew install openjdk` on a Mac.

Related tools (not required)
- You might like to install [VLC](https://www.videolan.org/vlc/) to view
  the downloaded videos, e.g. `brew cask install vlc` on a Mac.
- You'll probably want [`curl`](https://ec.haxx.se), but you already have that.

## Prerequisites on your Hikvision device(s)

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

## Usage

Hikvision Download Assistant is simple command line tool that connects to your Hikvision camera or NVR
using the ISAPI web API to perform searches for videos and photos. It writes `curl` commands to the screen
that you can then use to download each photo or video from the ISAPI API.

Like most command-line tools, it is designed to be easily composed with other command-line tools.
The list of search results goes to stdout.
Other helpful text is sent to stderr so it can be easily excluded from pipes and redirects.

Run using `java -jar target/hikvision-download-assistant-1.0-SNAPSHOT-jar-with-dependencies.jar <options>`.

## Compatible cameras and DVR/NVRs

This was developed and tested using my Hikvision model DS-2CD2185FWD-I IP camera
using Firmware version `V5.6.3 build 190923`.

Theoretically, it should work with any Hikvision camera.

I believe that the Hikvision NVRs offer the same ISAPI endpoint (`POST /ISAPI/ContentMgmt/search`) so
it should theoretically work with those too.

You mileage may vary. Github issues and PRs are welcome.

## Why?

Why, you ask? Several reasons!

- The Hikvision in-browser web UI software for browsing and downloading videos is *no
  longer compatible with MacOS at all*. When you open their web UI, the "Live View" 
  and "Configuration" tabs will appear, but the "Picture" and "Playback" tabs will not appear.
- The Hikvision in-browser web UI software for browsing and downloading videos for
  Windows works but still requires installing Hikvision software on your computer.
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

## Building

If you would prefer to compile the source code yourself, you'll need to install a Java JDK, 
e.g. `brew install openjdk` on a Mac.

To build, run `./mvnw clean package` from the top-level directory of the project.

The compiler will output a file called `target/hikvision-download-assistant-1.0-SNAPSHOT-jar-with-dependencies.jar`.
If you'd like, you can copy this to whatever directory and filename you like.

## Copyright and Licence

`Copyright (c) 2020 Ryan Richard`

Open Source and licensed under MIT. See [LICENSE](LICENSE) for license.
