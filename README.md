# OurSQL • ![Test status](https://img.shields.io/github/workflow/status/cryptoAlgorithm/OurSQL/test?label=test&style=for-the-badge) ![Build status](https://img.shields.io/github/workflow/status/cryptoAlgorithm/OurSQL/build?style=for-the-badge)


> An extensible graphical frontend to popular databases
> including MongoDB, MySQL, PostgreSQL and others.

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

### Bleeding Edge
[![Download nightly](https://img.shields.io/badge/download-nightly-blue?style=for-the-badge)](https://nightly.link/cryptoAlgorithm/OurSQL/workflows/build.yaml/main/OurSQL.jar.zip)

These builds are hot from the oven, built from the latest commit on
the `main` branch.

### Releases
Stable releases will be available soon.

---
### Running
Requires Java >= 17 installed on your system.

#### Windows
If a compatible version of Java is installed system-wide, you'll
simply need to double-click the downloaded JAR to open it.

#### macOS
Due to licencing issues, macOS will launch the JAR with Java 8
or earlier when double-clicked. Instead, run the command below
in Terminal.app to launch the JAR: 
```zsh
java -jar "[path to downloaded JAR]"
```
_Substitute the actual path of the JAR in the command_

Check your installed Java version with `java --version` - Only
Java 17 and above is supported.

#### Linux
Untested, but running the same command as macOS should also start
the app without much fuss.

## Documentation
Symbols above or equal to `package` visibility are 100% documented,
with clear and concise JavaDoc for descriptions, parameters,
returns and more. The docs are built by GitHub Actions for every
commit, and [available online](https://cryptoalgorithm.github.io/OurSQL/).
