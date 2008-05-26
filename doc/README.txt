= Vim Helpfile Doclet =
David Copeland <davetron5000@NOSPAM.gmail.com>
v1.0, Nov 2007

The Vim Doclet is a Doclet for Java 1.4 and above that generates documentation in the VIM Help File format. This format is a very rudimentary form of hypertext and allows a vim user to call up the documentation on a Java class in the same manner as searching the VIM help. This allows the developer to read documentation without exiting VIM.

== Trying it Out ==

Before installing and running, go to link:http://sourceforge.net/project/showfiles.php?group_id=210533[the files section] and download the sample zip. Unzip this in `+++~/.vim/doc+++`, run vim and type `:helptags +++~.vim/doc+++`. Then do `help String`. This will bring up the documentation for `java.lang.String`. The sample includes the JDK5 version of the `java.lang` and `java.util` packages.

image:vimdoclet1.png[Screenshot of what the documentation looks like]

== Installation == 

Download the latest distribution at link:http://sourceforge.net/projects/vimdoclet/[the SourceForge project page]. The distribution contains both source and binary (it's only one source file).  As this is a custom doclet, installation is largely just putting the `vimdoclet.jar` into your classpath before running javadoc. How you do this depends highly on your development environment.

== Running == 

The doclet takes two parameters, one of which is required:

outputDir:: 
    This is the location of a directory that should contain the generated documentation. You may want to put this to a temp directory first, however if you set this to `+++~/.vim/doc+++` this would be the easiest way to make the documentation available to vim

lineLength:: 
    This defaults to 80 and specifies the number of characters per line for the documentation. You can use this to increase the size of your documentation if you know you will be using vim in a particular size.

Aside from simply configuration `javadoc` to use this doclet, you also need source to run it on. The most useful source is the source code for Java itself. Fortunately, this is available from Sun link:https://jdk6.dev.java.net/[here (click on "Latest JDK 6 Source Snapshots")]. Once you download this, extract it somewhere and have your javadoc point to that (though you will need to point it to `j2se/src/share/classes` unless you want a ton of superfluous classes documented).

=== Running from Ant ===

The `run.xml` file included in the distribution has a target called `run` that will run the doclet on java source code. It is set up assuming you are running on the JDK source, as it will skip certain superfluous classes and delete the documentation for `java.awt.List` (see below). To use it, specify two parameters on the command line to ant as properties:

* *vimdoclet.source.root* - This is the root of the source, presumably the JDK. If you downloaded the JDK source, you should point this to `$JDK_SOURCE_ROOT/j2se/src/share/classes` as the JDK source package contains a lot more than what you probably want to document
* *vimdoclet.outputDir* - This is the *outputDir* command line to the doclet and is where the generated source will go

== Using the Documentation == 

Once you've generated the documentation, you need to generate Vim's tag index. If you've put your documents in `+++~/.vim/doc+++`, you can just do `:helptags +++~/.vim/doc+++` from within vim and, after a very long time of indexing your documentation is ready for use. Try it by doing `:help String` in vim.

If you've never used the vim help browser, highlighted text typically represents hyperlinks that can be followed by +++^]+++ (control and right bracket). You can navigate "back" via +++^T+++ (control and T).

See the vim documentation for further ways in which you can streamline your experience.

=== Possible Issues ===

The main issue you may find with this documentation is in the way Vim handles classes with the same name (e.g. `java.util.Date` vs `java.sql.Date`). Basically, it doesn't. If you do `:help Date` you will always go to `java.sql.Date`. You can to `help java.util.Date`, however vim doesn't know which Date you need to lookup without the entire package.

I find it useful to remove the documentation for `java.awt.List` because I almost never need it and find the documentation for `java.util.List` much more useful.

== Links ==

* link:https://jdk6.dev.java.net/[JDK6 Dev page (source available here)]
* link:http://www.vim.org[VIM Homepage]
* link:http://java.sun.com/j2se/javadoc/[Javadoc homepage]
* link:http://vim.wikia.com/wiki/Vim_Doclet[Vim wikia entry]

== Screenshots ==
image:vimdoclet1.png[Screenshot of what the documentation looks like]
image:vimdoclet2.png[Screenshot of the method list]
image:vimdoclet3.png[Screenshot a method's javadoc]

''''

image:sflogo.png[link="http://www.sourceforge.net"]
