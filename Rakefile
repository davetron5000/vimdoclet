
BUILD_DIR="build"
SRC_DIR = "src/java"
JAR_FILE=BUILD_DIR + "/vimdoclet.jar"
JAVA_FILES = FileList.new(SRC_DIR + "/**/*.java")
CLASSES_DIR = BUILD_DIR + "/classes"
DOC_DIR="doc"
HTML_DOC = DOC_DIR + "/index.html"
ASCII_DOC = DOC_DIR + "/README.txt"

desc "Creates a jar of the classes"
task :jar => :compile do
    classes = FileList.new(CLASSES_DIR + "/**/*.class")
    if (!uptodate?(JAR_FILE,classes))
        system "jar cvf #{JAR_FILE} " + classes.to_s or exit -1
    end
end

file CLASSES_DIR => JAVA_FILES do |t|
    compile_us = []
    t.prerequisites.each() { |src|
        class_file = src.pathmap("%{^" + SRC_DIR + "," + CLASSES_DIR + "}X.class")
        if (!uptodate?(class_file,src))
            compile_us << src
        end
    }
    if (compile_us.length > 0)
        puts "Compiling " + compile_us.join(" ")
        mkdir_p(CLASSES_DIR)
        system "javac -g -d #{CLASSES_DIR} " + compile_us.join(" ") or exit -1
    end
end

desc "Compiles all code"
task :compile => CLASSES_DIR

desc "Removes all build artifacts"
task :clean do
    rm_rf(BUILD_DIR)
    rm(DOC_DIR + "/index.html")
end

desc "Runs the doclet on the test class"
task :testrun => :jar do
    puts "testurn"
end

desc "Creates HTML from asciidoc"
task :doc => HTML_DOC

desc "Publish HTML to sourceforge"
task :docpush => :doc do
    puts "docpush"
end

desc "Create release tarball and zip file"
task :dist => [:clean,:jar,:doc] do
    puts "Dist"
end

desc "Create samples distribution"
task :distsamples do
    puts "distsamples"
end

task :default => :jar

file HTML_DOC => ASCII_DOC do |t|
    system "asciidoc -a toc -a numbered -o #{t.name} #{t.prerequisites[0]}" or exit -1
end

