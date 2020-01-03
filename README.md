
[DEPRECATED] Sadly, this project was never maintained in the first place. 

# Description

A long time ago, when I was cursing at `sbt`'s obscurity for the `n`'th time I said to myself: 

>This can't be right. There must be a simpler way to do this.  
>**I'm gonna create my own build tool for Scala.**

And so I did. Or at least I tried. I created this project - **FSBT - A Fast Scala Build Tool**. The premises were very simple and small in scope:

- simplicity, user friendliness
- speed, thanks to the use of fast scala compiler (fsc, zinc)
- easy of configuration (single file config)
- gradle-like dependency resolving

That was in 2016. I was also looking for a topic of my master's thesis. The goals aligned. 
And so, a year and a half later, the thesis was complete. You can't really say that about the build tool, because a project like that is never complete.
So let's just say it was working enough to write a paper about it.  

## How to create a build tool

I finished my degree and decided never to touch build tools again. Why? Because it's hard. It's tedious. There aren't many resources out there how to create them. I was basically coming up with concepts on how stuff could work from scratch and trying to implement it. You can read the official docs of existing tools, but more often than not they explain how to use stuff, rather than how they work. You can dig right into gradle's or sbt's source code (good luck with that) or ask other people around. I have to thank Chris who's behind [cbt](https://github.com/cvogt/cbt/) for helping me out a ton with starting out. But still, you're mostly on your own. So to make your like easier I'm including my thesis into this repo, for better or worse. It's... not a good thesis to begin with. I just wanted to finish the damn degree and move on. But it does explain a lot of stuff I ran into and managed to solve, so I hope it can be of some help to someone. 
There you go:

[Context-aware build automation system for Scala.pdf](./fsbt-paper.pdf)

## Please, just use one of these:

It's 2020 when I'm writing this, and so the tooling options for Scala are much greater than ever.

- [Mill](https://github.com/lihaoyi/mill): Li Haoyi, one of the top contributors to the Scala ecosystem released his own build while I was creating this. Let's just say this was the final nail in the coffin, and seeing how far ahead he was in every possible aspect made me decide to abandon this project altogether.
- [Bazel](https://github.com/bazelbuild/bazel) A fairly mature multi-language build-tool with plenty of corporate clients.
- [sbt](https://www.scala-sbt.org/) Yes, it's still just as complex as it was. But it's much faster nowadays, and if you learn to use it as a shell instead of a CLI it's actually really nice. Most of the time anyway.  
- [Gradle](https://gradle.org/) Hey, gradle still works fine with Scala. If you still hate sbt and like to keep things familiar, go for it.

