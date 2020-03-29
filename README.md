# ERP3 Prize Pool Generator
This is the tool (made with Kotlin/JS) that was used to generate the prize pool in the credits video for MightyTeapot's Elitist Raiding Party (ERP) 3 tournament.

## Preview
You can see it in action in the credits video around 3:33:

[![ERP3 credits video](https://img.youtube.com/vi/bYNl3eujm4Q/0.jpg)](https://youtu.be/bYNl3eujm4Q?t=213) 

## How it works
The tool uses the [Guild Wars 2 API](https://wiki.guildwars2.com/wiki/API:Main) in order to grab the name, icon, and
price of all the items that were donated. The list of donations is simply supplied through a `<textarea>`.

## Running
To run, just execute the following:
```commandline
gradlew run
```
Your browser should automatically open.
If you pass the `--continuous` flag, then code changes will automatically be compiled and after that the page will be refreshed.

## Building
To build a version for distribution, execute the following:
```commandline
gradlew browserProductionWebpack
```
The build will be placed in `./build/distributions`.