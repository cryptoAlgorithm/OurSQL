# OurSQL

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
Want to level up your database experience? Head over to releases
to download the latest prebuilt JAR! (Coming soon!)

## Documentation
Coming soon! For the time being, you can read the JavaDoc in the
source code.