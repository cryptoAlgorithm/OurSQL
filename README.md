# OurSQL • ![Test status](https://img.shields.io/github/workflow/status/cryptoAlgorithm/OurSQL/test?label=test&style=for-the-badge) ![Build status](https://img.shields.io/github/workflow/status/cryptoAlgorithm/OurSQL/build?style=for-the-badge)

> An extensible graphical frontend to popular databases
> including MongoDB, MySQL, PostgreSQL and others.

![Main UI](https://user-images.githubusercontent.com/64193267/194707607-b652d30b-9fcf-4971-ad1b-94ba563d7f8e.png)

## About
OurSQL - a better database experience for everyone, newcomers
and power users alike.

## Rant
Built in Java and JavaFX, the worst combination. Apps built
in JavaFX comes with the non-native look of Electron, without
the performance and developer experience of web apps. Java
screams of legacy code, and so many bad choices are the result
of trying to keep companies, with internal systems built 10 years
ago and never updated since, happy. With such a horrific 
developer experience, with some compiled languages light-years
ahead in both DX and performance, I have no idea why one would
write anything in Java, much less teach that in a curriculum.

Now, lets move on to JavaFX. It's built and marketed as a
"Swing replacement" by Oracle themselves, but is nowhere close in
its look and feel. With the "new" modena stylesheet (released in 
2014, hardly what I'd call modern), apps look horrible on _all_
platforms, now that's what I'd call cross-platform. Not to mention, 
you have to ship your application in the form of a JAR file, which
requires some black magic to get working, especially if you're using
JavaFX. For your application to be truly stand-alone, you'll have
to bundle a whole JRE in your installer or similar and use that to
run the JAR that you painstakingly created to fulfil the requirement
of the grading rubrics. That's the approach that many commercial
apps like IntelliJ or Minecraft take, which completely negates
any other gain that you might get using Java.

For apps to be anywhere near presentable, developers need to write
200+ lines of CSS to override nearly every style. And that's just
for one platform, unless you're fine with the app sticking out
like a sore thumb on other platforms. Come on, Java, SwiftUI did
it right on the first try, why isn't JavaFX, AWT, Swing or the
bazillion other crappy Java UI frameworks anywhere close?

Java: Develop once, tear out your hair everywhere.

## Releases
Want to level up your database experience? Download one of these
builds now!

### GitHub Releases
![GitHub release](https://img.shields.io/github/v/release/cryptoAlgorithm/OurSQL?style=for-the-badge)

Click on the badge above to go to the latest release to download a
prebuilt JAR. These builds are very stable, and the JAR is guranteed
to work (at least on Windows and macOS `aarch64`). However, releases
are only made periorically and do not include the latest bleeding-edge
features.

### Bleeding Edge
[![Download nightly](https://img.shields.io/badge/download-nightly-blue?style=for-the-badge)](https://nightly.link/cryptoAlgorithm/OurSQL/workflows/build.yaml/main/OurSQL.jar.zip)

These builds are hot from the oven, built from the latest commit on
the `main` branch. _**Note**: These builds are completely untested,
and not guaranteed to even launch properly._

---
### Running
Requires Java >= 17 installed on your system.

#### Windows
If a compatible version of Java is installed system-wide, you'll
simply need to double-click the downloaded JAR to open it. If that
doesn't work, follow [troubleshooting](#troubleshooting) steps
below.

#### macOS
By default, macOS comes preinstalled with Java 8 (or earlier), which
is not compatible with OurSQL. You'll need to install a newer version
of Java, preferably Java 17 (or later), following the instructions
above. Once you've installed a compatible version of Java, you'll
be able to simply double-click the JAR to start OurSQL. If that
doesn't 

#### Linux
Running the JAR on Linux is unsupported at the moment due to the
additional libraries required.

### Troubleshooting
OurSQL not starting? Try these steps in order:
1. Open a terminal and run `java -version`. If you see a version
   number above Java 17, you're good to go. If not, you'll need
   to install Java 17 (or later) on your system. Remember to reboot
   after installing Java for good measure.
2. Run `java -jar <path to OurSQL.jar>` in the terminal. If you
   see an error message, please open an issue on GitHub and
   include the error message. If OurSQL launches, congratulations!

## Documentation
Symbols above or equal to `package` visibility are 100% documented,
with clear and concise JavaDoc for descriptions, parameters,
returns and more. The docs are built by GitHub Actions for every
commit, and [available online](https://cryptoalgorithm.github.io/OurSQL/).
