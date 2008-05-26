/*
 *  Copyright (C) 2007 David Copeland, All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 */
package com.naildrivin5.applications.vimdoclet;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.sun.javadoc.*;

public class Main 
{
    private static final String LINE_LENGTH_OPTION_NAME = "-lineLength";
    private static final String OUTPUT_DIR_OPTION_NAME = "-outputDir";
    private static Pattern htmlTagPattern;
    private static Pattern preTagPattern;
    private static Pattern preCloseTagPattern;
    private static Pattern entityPattern;
    private static Pattern ampersandPattern;
    private static Pattern gtPattern;
    private static Pattern ltPattern;

    private static int lineLength = 80;
    private static String outputDir = ".";

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }

    public static boolean start(RootDoc root) 
    {
        readOptions(root.options());
        htmlTagPattern = Pattern.compile("<[^>]+>");
        preTagPattern = Pattern.compile("<[Pp][Rr][Ee]>");
        preCloseTagPattern = Pattern.compile("</[Pp][Rr][Ee]>");
        entityPattern = Pattern.compile("\\&[^; ]+[; ]");
        ampersandPattern = Pattern.compile("\\&amp;");
        gtPattern = Pattern.compile("\\&gt;");
        ltPattern = Pattern.compile("\\&lt;");
        try
        {
            for (ClassDoc doc: root.classes())
            {
                createVimHelp(doc);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    public static int optionLength(String option)
    {
        if (option == null)
            return 0;
        System.err.println(option);
        if (option.equals(OUTPUT_DIR_OPTION_NAME))
            return 2;
        if (option.equals(LINE_LENGTH_OPTION_NAME))
            return 2;
        return 0;
    }

    private static void readOptions(String[][] options)
    {
        String lineStr = null;
        String dirStr = null;
        for (int i=0;i<options.length; i++)
        {
            String []opt = options[i];
            if (opt[0].equals(OUTPUT_DIR_OPTION_NAME))
            {
                dirStr = opt[1];
            }
            else if (opt[0].equals(LINE_LENGTH_OPTION_NAME))
            {
                lineStr = opt[1];
            }
        }
        if (lineStr != null)
        {
            try
            {
                int len = Integer.parseInt(lineStr);
                lineLength = len;
            }
            catch (NumberFormatException e) 
            { 
                System.err.println(lineStr + " is not a valid number, defaulting to " + lineLength);
            }
        }
        if (dirStr != null)
        {
            File dir = new File(dirStr);
            if (dir.exists())
            {
                if (dir.canWrite())
                {
                    outputDir = dirStr;
                }
                else
                {
                    System.err.println(dirStr + " is not writable, defaulting to " + outputDir);
                }
            }
            else
            {
                System.err.println(dirStr + " does not exist, defaulting to " + outputDir);
            }
        }
    }

    private static void createVimHelp(ClassDoc doc)
        throws Exception
    {
        String filename = getFilename(doc);

        PrintWriter w = new PrintWriter(new File(outputDir + File.separator + filename));

        writeHeader(w,doc);
        writeSummary(w,doc);
        writeFieldLinks(w,doc);
        writeMethodLinks(w,doc);
        writeDescription(w,doc);
        writeFields(w,doc);
        writeMethods(w,doc);

        w.close();
    }

    private static void writeFieldLinks(PrintWriter w, ClassDoc doc)
    {
        FieldDoc fields[] = doc.fields(true);
        Arrays.sort(fields);
        if (fields.length == 0)
            return;
        writeSection(w,doc,"Fields");

        for (FieldDoc d: fields)
        {
            w.print("|");
            w.print(createFieldLinkName(d));
            w.println("|");
        }
        w.println("");
    }

    private static void writeFields(PrintWriter w, ClassDoc doc)
    {
        FieldDoc fields[] = doc.fields(true);
        Arrays.sort(fields);
        if (fields.length == 0)
            return;

        for (FieldDoc d: fields)
        {
            String link = createFieldLinkName(d);
            w.print("*");
            w.print(link);
            w.println("*");
            w.println("");
            outputTags(w,d.inlineTags());
            w.println("");
        }
        w.println("");
    }

    private static void writeMethods(PrintWriter w, ClassDoc doc)
    {
        ConstructorDoc constructors[] = doc.constructors(true);
        Arrays.sort(constructors);
        if (constructors.length != 0)
        {
            for (ConstructorDoc d: constructors)
            {
                String link = createMethodLinkName(d);
                w.print("*");
                w.print(link);
                w.println("*");
                w.println("");

                w.print(d.modifiers());
                w.print(" ");
                w.print(d.name());
                w.print("(");
                Parameter parameters[] = d.parameters();
                if (parameters.length == 0)
                    w.println(")");
                else if (parameters.length > 1)
                    w.println("");
                for (int i=0;i<parameters.length; i++)
                {
                    Parameter p = parameters[i];
                    if (parameters.length > 1)
                        w.print("  ");
                    w.print(p.type().toString());
                    w.print(" ");
                    w.print(p.name());
                    if (i < (parameters.length - 1))
                        w.println(",");
                    else
                        w.println(")");
                }

                ClassDoc exceptions[] = d.thrownExceptions();
                Arrays.sort(exceptions);

                if (exceptions.length > 0)
                {
                    w.print("  throws ");
                    for (ClassDoc ex: exceptions)
                    {
                        w.print("|");
                        w.print(ex.qualifiedTypeName());
                        w.println("|");
                        w.print("         ");
                    }
                }
                w.println("");

                outputTags(w,d.inlineTags());

                ParamTag params[] = d.paramTags();
                for (ParamTag t: params)
                {
                    w.print("    ");
                    w.print(t.parameterName());
                    w.print(" - ");
                    w.print(format(stripHtml(t.parameterComment()),"       "));
                }
                w.println("");

            }
        }

        MethodDoc methods[] = doc.methods(true);
        Arrays.sort(methods);
        if (methods.length == 0)
            return;

        for (MethodDoc d: methods)
        {
            String link = createMethodLinkName(d);
            w.print("*");
            w.print(link);
            w.println("*");
            w.println("");

            w.print(d.modifiers());
            w.print(" ");
            Type returnType = d.returnType();
            if (!returnType.isPrimitive())
                w.print("|");
            w.print(d.returnType().qualifiedTypeName());
            if (!returnType.isPrimitive())
                w.print("|");
            w.print(getNonLinkableTypeInfo(d.returnType()));
            w.print(" ");
            w.print(d.name());
            w.print("(");
            Parameter parameters[] = d.parameters();
            if (parameters.length == 0)
                w.println(")");
            else if (parameters.length > 1)
                w.println("");
            for (int i=0;i<parameters.length; i++)
            {
                Parameter p = parameters[i];
                if (parameters.length > 1)
                    w.print("  ");
                w.print(p.type().toString());
                w.print(" ");
                w.print(p.name());
                if (i < (parameters.length - 1))
                    w.println(",");
                else
                    w.println(")");
            }

            ClassDoc exceptions[] = d.thrownExceptions();
            Arrays.sort(exceptions);

            if (exceptions.length > 0)
            {
                w.print("  throws ");
                for (ClassDoc ex: exceptions)
                {
                    w.print("|");
                    w.print(ex.qualifiedTypeName());
                    w.print("|");
                    w.println(getNonLinkableTypeInfo(ex));
                    w.print("         ");
                }
            }
            w.println("");

            outputTags(w,d.inlineTags());

            writeDeprecation(d,"    ",w);

            ParamTag params[] = d.paramTags();
            for (ParamTag t: params)
            {
                w.print("    ");
                w.print(t.parameterName());
                w.print(" - ");
                w.print(format(stripHtml(t.parameterComment()),"       "));
            }
            w.println("");

            Tag tags[] = d.tags("@return");
            for (Tag t: tags)
            {
                w.print("    Returns: ");
                w.print(format(stripHtml(t.text()),"             "));
            }
            if (tags.length > 0)
                w.println("");

        }
        w.println("");
    }

    private static String createFieldLinkName(FieldDoc d)
    {
        return d.type() + "_" + d.qualifiedName();
    }

    private static void writeMethodLinks(PrintWriter w, ClassDoc doc)
    {
        ConstructorDoc constructors[] = doc.constructors(true);
        Arrays.sort(constructors);
        if (constructors.length != 0)
        {
            writeSection(w,doc,"Constructors");
            for (ConstructorDoc d: constructors)
            {
                String link = createMethodLinkName(d);
                w.print("|");
                w.print(link);
                w.print("|");
                int length = lineLength - link.length() - 3;
                if (length >= 0)
                {
                    StringBuilder s = new StringBuilder(getFirstSentence(d).replace('\n',' ').trim());
                    if (s.length() > length)
                    {
                        s.setLength(length);
                    }
                    w.println(s.toString());
                }
                else
                {
                    w.println("");
                }
            }
            w.println("");
        }

        MethodDoc methods[] = doc.methods(true);
        Arrays.sort(methods);
        if (methods.length == 0)
            return;

        writeSection(w,doc,"Methods");

        for (MethodDoc d: methods)
        {
            String link = createMethodLinkName(d);
            w.print("|");
            w.print(link);
            w.print("|");
            int length = lineLength - link.length() - 3;
            if (length >= 0)
            {
                StringBuilder s = new StringBuilder(getFirstSentence(d).replace('\n',' ').trim());
                if (s.length() > length)
                {
                    s.setLength(length);
                }
                w.println(s.toString());
            }
            else
            {
                w.println("");
            }
        }

        w.println("");
    }

    private static void writeSection(PrintWriter w, ClassDoc doc, String section)
    {
        w.print("*");
        w.print(getSectionName(doc,section));
        w.println("*");
    }

    private static String getSectionName(ClassDoc doc,String section)
    {
        return doc.qualifiedName() + "_" + section.replace(' ','_');
    }

    private static String createMethodLinkName(ExecutableMemberDoc d)
    {
        String s = d.qualifiedName() + d.flatSignature();
        return s.replace(" ","");
    }

    private static void writeSummary(PrintWriter w, ClassDoc doc)
    {
        ClassDoc superDoc = doc.superclass();

        w.print(doc.modifiers());
        w.print(" ");
        w.print(doc.isInterface() ? "interface " : doc.isEnum() ? "enum" : "class ");
        w.print(doc.typeName());

        TypeVariable typeVars[] = doc.typeParameters();
        if (typeVars.length > 0)
        {
            System.err.println(doc.toString() + " had a type var");
            w.print("<");
            for (int i=0;i<typeVars.length; i++)
            {
                TypeVariable t = typeVars[i];
                w.print(t.toString());
                if (i < (typeVars.length - 1))
                    w.print(",");
            }
            w.print(">");
        }
        w.println("");

        if (superDoc != null)
        {
            w.print("  extends    |");
            w.print(superDoc.qualifiedTypeName());
            w.print("|");
            w.print(getNonLinkableTypeInfo(superDoc));
        }
        w.println("");
        
        ClassDoc interfaces[] = doc.interfaces();
        if (interfaces.length > 0)
        {
            w.print("  implements ");
            for (int i=0;i<interfaces.length; i++)
            {
                ClassDoc d = interfaces[i];
                w.print("|");
                w.print(d.qualifiedTypeName());
                w.print("|");
                w.println(getNonLinkableTypeInfo(d));
                if (i < (interfaces.length - 1) )
                    w.print("             ");
            }
        }
        w.println("");

        w.print("|");
        w.print(getSectionName(doc,"Description"));
        w.println("|");

        w.print("|");
        w.print(getSectionName(doc,"Fields"));
        w.println("|");

        w.print("|");
        w.print(getSectionName(doc,"Constructors"));
        w.println("|");

        w.print("|");
        w.print(getSectionName(doc,"Methods"));
        w.println("|");
        w.println("");
        for (int i=0;i<lineLength;i++)
        {
            w.print("=");
        }
        w.println("");
        w.println("");
    }

    private static String getFilename(ClassDoc doc)
    {
        return doc.qualifiedTypeName() + ".txt";
    }

    private static void writeDescription(PrintWriter w, ClassDoc doc)
    {
        writeSection(w,doc,"Description");
        w.println("");
        Tag tags[] = doc.inlineTags();
        outputTags(w,tags);
        w.println("");
        writeDeprecation(doc,"  ",w);
    }

    private static void writeDeprecation(Doc doc, String linePrefix, PrintWriter w)
    {
        Tag dep[] = doc.tags("@deprecated");
        if (dep != null)
        {
            for (Tag tag: dep)
            {
                w.print(linePrefix);
                w.print("Deprecated");
                if ( (tag.text() != null) && (tag.text().trim().length() > 0) )
                    w.println(": " + tag.text());
                else
                    w.println("");
            }
            w.println("");
        }
    }

    private static void outputTags(PrintWriter w, Tag tags[])
    {
        StringBuilder s = new StringBuilder();
        for (Tag t: tags)
        {
            if (t instanceof SeeTag)
            {
                SeeTag seeTag = (SeeTag)t;

                s.append(" ");
                s.append(seeTag.label());
                s.append("(|");
                s.append(seeTag.referencedClassName());
                s.append("|) ");
            }
            else
            {
                s.append(stripHtml(t.text()));
            }
        }
        w.println(format(s.toString()));
    }

    private static String format(String s)
    {
        return format(s,"");
    }
    private static String format(String s, String pre)
    {
        StringBuilder b = new StringBuilder("");
        String paragraphs[] = s.split("\n *\n+");
        for (String o: paragraphs)
        {
            o = o.replaceAll("[\n ]+"," ");
            b.append(wrap(o,lineLength,pre));
            b.append("\n\n");
        }
        if (paragraphs.length > 0)
            b.setLength(b.length() - 1);
        return b.toString();
    }

    private static void writeHeader(PrintWriter w, ClassDoc doc)
    {
        StringBuilder b = new StringBuilder("*");
        b.append(doc.qualifiedTypeName());
        b.append("* *");
        b.append(doc.name());
        b.append("* ");
        w.print(b.toString());
        int len = b.length();
        if (len < lineLength) // we have space left
        {
            int spaceLeft = lineLength - len;
            b.setLength(0);
            b.append(getFirstSentence(doc));
            if (b.length() > spaceLeft)
            {
                b.setLength(spaceLeft);
            }
            w.println(b.toString());
        }
        else
        {
            w.println("");
        }
        w.println("");

    }

    private static String getFirstSentence(Doc d)
    {
        Tag tags[] = d.firstSentenceTags();
        StringBuilder b = new StringBuilder("");

        if (tags != null)
        {
            for (Tag t: tags)
            {
                b.append(stripHtml(t.text()));
            }
        }
        return b.toString();
    }

    private static String stripHtml(String s)
    {
        if (s == null) return null;
        s = s.trim();


        Matcher mPre = preTagPattern.matcher(s);
        s = mPre.replaceAll("\n");

        Matcher mPreClose = preCloseTagPattern.matcher(s);
        s = mPreClose.replaceAll("\n");

        Matcher m = htmlTagPattern.matcher(s);
        s = m.replaceAll("");

        Matcher amp = ampersandPattern.matcher(s);
        s = amp.replaceAll(" and ");

        Matcher gt = gtPattern.matcher(s);
        s = gt.replaceAll(">");

        Matcher lt = ltPattern.matcher(s);
        s = lt.replaceAll("<");

        Matcher entity = entityPattern.matcher(s);
        s = entity.replaceAll("");

        return s;
    }

    private static String wrap(String s, int lineLength, String pre)
    {
        if (pre == null) pre = "";
        String words[] = s.split(" ");
        StringBuilder overallString = new StringBuilder("");
        StringBuilder currentLine = new StringBuilder("");

        for (String w: words)
        {
            w = w.trim();
            if (w.length() == 0)
            {
                continue;
            }
            // guaranteed to wrap
            if (w.length() >= lineLength)
            {
                // if we have nothing on this line, write this one out
                if (currentLine.length() == 0)
                {
                    overallString.append(w);
                }
                else // write what we have and THEN this one
                {
                    overallString.append(currentLine.toString());
                    overallString.append("\n");
                    overallString.append(w);
                }
                overallString.append("\n");
                currentLine.setLength(0);
                currentLine.append(pre);
            }
            else 
            {
                // if we would go over our line length by appending 
                if ( (currentLine.length() + w.length() + 1) > lineLength )
                {
                    // write out what we have and start a new line
                    overallString.append(currentLine.toString());
                    overallString.append("\n");
                    currentLine.setLength(0);
                    currentLine.append(pre);
                }
                currentLine.append(w);
                currentLine.append(" ");
            }
        }
        if (currentLine.length() > pre.length())
            overallString.append(currentLine.toString());
        return overallString.toString();
    }
    private static String getNonLinkableTypeInfo(Type t)
    {
        StringBuilder returnMe = new StringBuilder(t.dimension());

        if (t instanceof ParameterizedType)
        {
            ParameterizedType param = t.asParameterizedType();
            if (param != null)
            {
                Type args[] = param.typeArguments();
                if (args.length > 0)
                {
                    StringBuilder b = new StringBuilder("<");
                    for (Type type: args)
                    {
                        if (type instanceof WildcardType)
                        {
                            b.append(type.toString());
                        }
                        else if (type instanceof TypeVariable)
                        {
                            b.append(type.toString());
                        }
                        else
                        {
                            b.append(type.simpleTypeName());
                        }
                        b.append(getNonLinkableTypeInfo(type));
                        b.append(",");
                    }
                    b.setLength(b.length() - 1);
                    b.append(">");
                    returnMe.append(b.toString());
                }
            }
        }
        return returnMe.toString();
    }
}

