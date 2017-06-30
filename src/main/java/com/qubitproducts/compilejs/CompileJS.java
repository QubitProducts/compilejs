/*
 *  Copyright  @ QubitProducts.com
 *
 *  CompileJS is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CompileJS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License.
 *  If not, see LGPL licence at http://www.gnu.org/licenses/lgpl-3.0.html.
 */
package com.qubitproducts.compilejs;

import com.qubitproducts.compilejs.Log.LogLevel;
import com.qubitproducts.compilejs.processors.JSWrapperProcessor;
import com.qubitproducts.compilejs.processors.JSTemplateProcessor;
import com.qubitproducts.compilejs.processors.JSStringProcessor;
import static com.qubitproducts.compilejs.MainProcessorHelper.chunkToExtension;
import com.qubitproducts.compilejs.fs.CFile;
import com.qubitproducts.compilejs.fs.FSFile;
import com.qubitproducts.compilejs.processors.InjectionProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter (Piotr) Fronc <peter.fronc@qubitproducts.com>
 */
public class CompileJS {

    private Map<String, List<String>> lineReaderCache = null;
    public static String MORE_ARGS = "\nNeed more arguments.\n"
        + "\n"
        + "Example:\n"
        + "\n"
        + "java -jar compilejs.jar -o out.js --info -v -s js/app/Main.js  --source-base "
        + "js/  --index --prefix \">>> \" --suffix \" <<<\"  -i .js,.jss  "
        + "  -dl \"Log.FINE\"\n"
        + "\n"
        + "Translates to:\n"
        + "  -Output to out.js\n"
        + "  -Display info\n"
        + "  -Verbosive (use -vv for very verbosive)\n"
        + "  -source file (can be directory) at js/app/Main.js\n"
        + "  -Source base for dependencies searching at js/\n"
        + "  -List index only (remove --index to merge contents instead)\n"
        + "  -Prefix is '>>> ' (--index only)\n"
        + "  -Suffix is ' <<<' (--index only)\n"
        + "  -Include files with .js and .jss extension only (remove for all files)\n"
        + "  -Exclude from files lines containing 'Log.FINE' (only when merging contents)\n"
        + "\n"
        + "Please use --help or -h for more information.";

    public static String USAGE = "CompileJS \n"
        + "================================================================================\n"
        + "\n"
        + "Summary\n"
        + "\n"
        + "CompileJS is a tool supporting dependency management JavaScript. It \n"
        + "has been created to allow developers to include dependencies and \n"
        + "CSS and HTML content in JS files. \n"
        + "CompileJs extends CompileJS functionality with per-extension\n"
        + "dependencies recognition (CSS, HTML). It also supports embeded string\n"
        + "templates for javascript, so HTML fragments can be in unchanged form.\n"
        + "CompileJS is lightweight and written purely in java.\n"
        + "Program is a great concatenating tool, especially when used for web development \n"
        + "build systems. It allows simply and efficiently merging JS or CSS dependencies \n"
        + "trees into single output files. Debug web pages can be easily created by using \n"
        + "CompileJS listing page options and release files can be optimized by using \n"
        + "powerful excluding patterns.\n"
        + "\n"
        + "================================================================================\n"
        + "\n"
        + "Details\n"
        + "\n"
        + "Program will merge or list files contents in specific order if dependencies\n"
        + "keywords are used, for example lines in a file named myFile.css:\n"
        + "\n"
        + "[...]\n"
        + "//:css css.style\n"
        + "[...]\n"
        + "//:include ../otherdir/license.txt\n"
        + "[...]\n"
        + "\n"
        + "will make program to order merged files contents as paths below: \n"
        + "\n"
        + "[srcBase]/css/style.css\n"
        + "[scrBase]/../otherdir/license.txt\n"
        + "[srcBase]myFile.css\n"
        + "\n"
        + "[srcBase] is runtime -cp/--src-base argument, and by default, it is \n"
        + "current directory. Dependency detection works recursively.\n"
        + "Program supports basic sprockets style dependencies addressing for JS files, \n"
        + "//=require path will be translated to //:include path.js\n"
        + "\n"
        + "Program also can filter contents by:\n"
        + "\n"
        + "- line of text (keyword, for example: //delete )\n"
        + "\n"
        + "- block of text (/*~keyword*/,<--~keyword--> etc.). Using .~keyword. will \n"
        + "filter:\n"
        + "    AAA\n"
        + "    .keyword.\n"
        + "    BBB\n"
        + "    .~keyword.\n"
        + "    CCC\n"
        + "to:\n"
        + "    AAA\n"
        + "\n"
        + "\n"
        + "\n"
        + "    CCC\n"
        + "\n"
        + "- entire file (keyword, for example, file containing: /**exclude this file**/)\n"
        + "\n"
        + "Content filtering is applied to final merged output file only (after merging \n"
        + "files set).\n"
        + "\n"
        + "Program can simply list files instead of merging its contents by using --index \n"
        + "option - in addition, you can use prefix and suffix for each file path in the\n"
        + "list to be prefixed/suffixed. Files list is same ordered like in merge process.\n"
        + "\n"
        + "When using CompileJS it is strongly recommended to specify source base and the\n"
        + "file where process starts from. Please see usage list for more details.\n"
        + "\n"
        + "================================================================================\n"
        + "\n"
        + " Usage:                                                               \n"
        + "                                                                      \n"
        + "  -i <include extensions - file ENDINGS, default: * (all)>            \n"
        + "      example: -i .js,.css,.xml (default: .js)                        \n"
        + "  -o <output file path> This argument must be specified.              \n"
        + "  --info Show final config summary(info)                              \n"
        + "  -s <src dir/file path> if it is not directory, --source-base mode is\n"
        + "     enabled. If it is directory, program will take as subject all  \n"
        + "     files from that directory and will treat it as a source base.    \n"
        + "  -ir ignore Require.js deps (default: false)                         \n"
        + "  --index It will ignore merging and generate prefix,suffix list      \n"
        + "  --prefix <prefix for index generation>                              \n"
        + "  --source-base comma separated source bases, if specified, all       \n"
        + "                dependencies will be searched with an order defined with\n"
        + "                this parameter.  Example: \"src, libs/src\"           \n"
        + "  --suffix <suffix for index generation>                              \n"
        + "  --not-relative <absolute paths index generation, default: false>    \n"
        + "  -vv very verbose                                                    \n"
        + "  -v verbose                                                          \n"
        + "  -nd <process no dependencies in files? see: //= and //:include>     \n"
        + "  -dl <cut lines containing strings(comma separated)>                    \n"
        + "   example: /*D*/ or /*X*/ (defaults: /*D*/,//=,//:include,//= require)\n"
        + "  -df <file exclude patterns, defaults:                               \n"
        + "   /****!ignore!****/,////!ignore!////,##!ignore!## (comma separated) \n"
        + "  -dw <wrapped text cut by strings(comma separated)                   \n"
        + "   example: /*start*/ <cut text> /*~start*/ in file, command line arg \n"
        + "   will be: -dw /*~start*/ (keep ~ unique, it's used to mark endings. \n"
        + " --parse-only-first-comment-dependencies for performance reasons      \n"
        + "   you may want to parse dependencies contents only for first lines   \n"
        + "   starting in a file as a comment (it means that program will     \n"
        + "   not go through file contents to analyse deps and only till         \n"
        + "   comment like contents is present)                                  \n"
        + " --add-base If this option is added and --index is used the file list \n"
        + "   index will have source base appended accordigly to where it is found.\n"
        + " --exclude-file-patterns If this option is specified, each comma       \n"
        + "   separated string will be tested with java regex method to match \n"
        + "   name of file. If any of strings match - file will be     \n"
        + "   excluded from processing.\n"
        + " --keep-lines If passed, stripping process will put empty lines in \n"
        + "              place of cut out ones. Default: false.\n"
        + " --unix-path If index listings should care for UNIX style output, "
        + "         default is: true\n"
        + " --exclude-file-path-patterns If this option is specified, each comma \n"
        + "   separated string will be tested with java regex method to match \n"
        + "   name of full file path. If any of strings match - file will be     \n"
        + "   excluded from processing.\n"
        + " --no-eol If set, and --index option is selected, no end of line will be \n"
        + "          added to the index list items.\n"
        + " --cwd Specify current working directory. Default is current directory.\n"
        + "       It does not affect -o property. Use it when you cannot manage CWD.\n"
        + " --no-file-exist-check if added, MM will NOT check if dependencies EXIST. \n"
        + "                   It also assumes that first class path is used ONLY - "
        + "                   first entry from --source-base will be used ONLY."
        + "\n"
        + " --chunk-extensions array, comma separated custom extensions used for wraps.\n"
        + " --only-cp Pass to make compilejs use only classpath baesed imports.\n"
        + "   Default: *~css*,*~htm*,*~" + JSTemplateProcessor.JS_TEMPLATE_NAME
        + "*  Those wrap definitions are used to take out\n"
        + "   chunks of file outside to output with extension defined by wrap keyword.\n"
        + "   For example: /*~c-wrap*/ chunk will be written to default OUTPUT \n"
        + "   (-o option) plus c-wrap extension. Its advised to use alphanumeric\n"
        + " characters and dash and underscore and dot for custom wraps.\n"
        + " --options compilejs specific options: \n"
        + "          css2js -> convert css output to javascript. The javascript\n"
        + "                   producing CSS will be inserted before all JS code.\n"
        + "          css2js-multiline -> Display js from css in mulitlines.\n"
        + "          wrap-js -> If javascript files should be wrapped in functions.\n"
        + "          html-output -> all files should be merged into one html\n"
        + "                         output file.\n"
        + "\n"
        + " --add-excluded-files   if some files must be absolutely excluded \n"
        + "                      list them comma separated.                  \n"
        + " --file-search-excluded Do not enter to directories (when directory \n"
        + "          specified).\n"
        + "          Pass java regex inside: _dname.* will cause any  \n"
        + "          directory name starting with _dname to be ignored.\n"
        + "          Note: this option does not apply for dependencies search.\n"
        + " --help,-h Shows this text                                        \n"
        + " --config [filename] Default file name is compilejs.properties \n"
        + " --watch If added, compilejs will repeat process each time specified\n"
        + "         source file/path file system tree change occurs.\n"
        + "================================================================================";

    public static final Logger LOGGER
        = Logger.getLogger(CompileJS.class.getName());

    public static PrintStream ps = System.out;

    List<String> sourcesPaths = null;
    
    private boolean verbose = false;
    private boolean vverbose = false;
    private final Log log;

    public CompileJS() {
      this.log = new Log();
    }
    
    private void logToConsole(String msg) {
        if (verbose || vverbose) {
            ps.print(msg);
        }
    }

    public static void printArgs() {
        ps.println(MORE_ARGS);
    }

    public static void printUsage() {
        ps.print(USAGE);
    }

    /**
     * Main function. See the usage blocks for args.
     *
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(final String[] args) 
        throws IOException, Exception {
        
        if (!new File(PROPERTY_FILE_NAME).exists() && (args.length == 0 || 
            args.length == 1 && args[0].equals("--help"))) {
          printUsage();
          return;
        }
        
        //add prop file reading
        final CompileJS compiler = new CompileJS();
        final Map<String, List<String>> cache =
            new HashMap<String, List<String>>();
        
        compiler.setLineReaderCache(cache);
        
        try {
            List<String> outputs = compiler.compile(args);

            String watches = getParamFromArgs(args, "--watch", null)[0];
            
            if (watches != null && outputs != null) {
                String[] paths = watches.split(",");
                for (int i = 0; i < paths.length; i++) {
                    paths[i] = paths[i].trim();
                }
                
                List<String> pathsList = 
                    cleanPaths(compiler.cwd, Arrays.asList(paths));
                
                if (pathsList.isEmpty()) {
                  throw new FileNotFoundException(
                          "None of source paths provided exists! Stopping.");
                }
                
                System.out.println(
                    "Watch option specified - watching sources...");

                for (String path : pathsList) {
                                        
                    //get all absolute paths
                    path = new CFile(compiler.cwd, path, true)
                        .getAbsolutePath();
                    //attach wather
                    new Watcher().watch(
                        path,
                        new Callback() {
                            @Override
                            public void call(Object o) {
                                try {
                                    cache.clear();
                                    compiler.compile(args);
                                } catch (Exception ex) {
                                    Logger.getLogger(CompileJS.class.getName())
                                    .log(Level.SEVERE, null, ex);
                                }
                            }
                        },
                        null,
                        outputs);
                }
            }
        } finally {
            //wil be released
            //cache.clear();
        }
    }

    public static String PROPERTY_FILE_NAME = "compilejs.config";
    private String cwd;

    public List<String> readConfig(String fname) {
        CFile file = new CFile(fname);
        if (!file.exists()) {
            return null;
        }

        try {
            String all = file.getAsString();
            String[] allOptions = null;

            if (all != null) {
                allOptions = all.split("\n");
            }

            List<String> list = new ArrayList<String>();

            if (allOptions != null) {
                for (String arg : allOptions) {
                    arg = arg.trim();

                    int spaceIdx = arg.indexOf(" ");
                    if (spaceIdx != -1) {
                        String name = arg.substring(0, spaceIdx);
                        String value = arg.substring(spaceIdx + 1);
                        if (!name.equals("--config")) {
                          list.add(name);
                          if (value != null && !value.equals("")) {
                              list.add(value);
                          }
                        }
                    } else {
                        list.add(arg);
                    }
                }
            }
            return list;
        } catch (Exception ex) {
            if (log.LOG) {
                error(ex);
                logToConsole(ex.getMessage());
            }
        }
        return null;
    }

    Callback callback = null;

    public void error(Exception e) {
        if (this.callback != null) {
            this.callback.call(e);
        }
    }

    public void onError(Callback c) {
        this.callback = c;
    }

    static public boolean isSetInArgs(
                String[] args,
                String name) {
        for (String arg : args) {
            if (arg.equals(name)) {
                return true;
            }
        }

        return false;
    }
    
    static public String[] getParamFromArgs(
        String[] args, String name, String _default) {
      List<String> params = new  ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && args[i].equals(name)) {
                if (i < args.length - 1) {
                  params.add(args[++i]);
                }
            }
        }

        if (params.isEmpty()) {
          params.add(_default);
        }
        return params.toArray(new String[]{});
    }

    String getCwdFromArgs(String[] args) throws IOException {
        String arg = getParamFromArgs(args, "--cwd", null)[0];
        if (arg != null) {
            return new CFile(arg).getCanonicalPath();
        }
        return arg;
    }

    /**
     * Compilation executor. Returns null only if process stopped at 
     * arguments validation and failed before execution. Otherwise, returns
     * outputs list (can be empty!).
     * @param args
     * @return
     * @throws IOException
     * @throws Exception 
     */
    public List<String> compile(String[] args) 
        throws FileNotFoundException, 
        CouldNotCreateOutputDirException, IOException {
        
        args = validateArrayForNulls(args);
        args = this.addConfigFromConfigFiles(args);
        args = validateArrayForNulls(args);

        if (cwd == null) {
            cwd = new CFile("").getAbsolutePath();
        }
        
        /// normal process, refactor it
        boolean exit = false;
        long start = System.nanoTime();
        long done = 0;
        /*
         * Initialise the arguments to be stored.
         */
        String filesIncluded = ".js,.css,.html,.htm,.xhtml,.xml,.json";
        String output = null;
        Boolean noEol = false;
        boolean info = false;
        boolean help = false;
        boolean relative = true;
        boolean onlyClasspath = false;
        boolean ignoreRJS = false;
        String srcString = ".";
        ArrayList<String> sourcesPathsList = new ArrayList<String>();
        
        List<String> sourceBase = new ArrayList<String>();
        String linesToExclude = null;
        String filesToExclude = null;
        String wrapsToExclude = null;
        boolean generateIndex = false;
        boolean unixPath = true;
        boolean dependencies = true;
        boolean keepLines = false;
        boolean parseOnlyFirstComments = false;

        String defaultPrefix = "<script type=\"text/javascript\" src=\"";
        String defaultSuffix = "\"></script>";

        Map<String, String> prefixPerExtension = new HashMap<String, String>();
        Map<String, String> suffixPerExtension = new HashMap<String, String>();

        boolean withSourceBase = false;
        String excludeFilePatterns = null;
        String excludeFilePathPatterns = null;
        boolean checkIfDependencyExistsOption = true;
        boolean perExtensions = true;
        boolean createDirsForOutput = false;

        ArrayList<String> excludedFiles = new ArrayList<String>();
        excludedFiles.add(PROPERTY_FILE_NAME);

        ArrayList<String> excludedListFiles = new ArrayList<String>();
        //--file-search-excluded

        String excludedFilesString = "";
        String excludedDirsString = "";

        List<String> defaltWraps = Arrays.asList(new String[]{
            "*~css*",
            "*~htm*",
            "*~" + JSTemplateProcessor.JS_TEMPLATE_NAME + "*",
            "*~" + JSStringProcessor.JS_TEMPLATE_NAME + "*"
        });

        MainProcessor mainProcessor = null;
        HashMap<String, String> options = new HashMap<String, String>();
        String eol = "\n";

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) {
                    filesIncluded = args[++i];
                } else if (args[i].equals("--keep-lines")) {
                    keepLines = true;
                } else if (args[i].equals("-o")) {
                    output = args[++i];
                } else if (args[i].equals("-s")) {
                    srcString = args[++i];
                    String[] sourceFiles = srcString.split(",");
                    for (String tmp : sourceFiles) {
                        sourcesPathsList.add(tmp);
                    }
                } else if (args[i].equals("--parse-only-first-comment-dependencies")) {
                    parseOnlyFirstComments = true;
                } else if (args[i].equals("--source-base")) {
                    String[] srcs = args[++i].split(",");
                    for (String src1 : srcs) {
                        String path = src1.trim();
                        if (!path.equals("")) {
                            sourceBase.add(path);
                        }
                    }
                } else if (args[i].equals("-cp")) {
                    String cp = args[++i];
                    String path = cp.trim();
                    if (!path.equals("")) {
                        sourceBase.add(path);
                    }
                } else if (args[i].equals("--info")) {
                    info = true;
                } else if (args[i].equals("-nd")) {
                    dependencies = false;
                } else if (args[i].equals("-ir")) {
                    ignoreRJS = true;
                } else if (args[i].equals("-dl")) {
                    if (linesToExclude == null) {
                      linesToExclude = "";
                    } else {
                      linesToExclude += ",";
                    }
                   linesToExclude += args[++i];
                } else if (args[i].equals("-df")) {
                    if (filesToExclude == null) {
                      filesToExclude = "";
                    } else {
                      filesToExclude += ",";
                    }
                   filesToExclude += args[++i];
                } else if (args[i].equals("-dw")) {
                  if (wrapsToExclude == null) {
                    wrapsToExclude = "";
                  } else {
                    wrapsToExclude += ",";
                  }
                   wrapsToExclude += args[++i];
                } else if (args[i].equals("--index")) {
                    generateIndex = true;
                } else if (args[i].equals("--prefix")) {
                    defaultPrefix = args[++i];
                } else if (args[i].equals("--suffix")) {
                    defaultSuffix = args[++i];
                } else if (args[i].startsWith("--prefix-")) {
                    ps.println(args[i]);
                    prefixPerExtension.put(
                        args[i].replaceFirst("--prefix-", ""),
                        args[i + 1]);
                } else if (args[i].startsWith("--suffix-")) {
                    suffixPerExtension.put(
                        args[i].replaceFirst("--suffix-", ""),
                        args[i + 1] + eol);
                } else if (args[i].equals("--not-relative")) {
                    relative = false;
                } else if (args[i].equals("-vv")) {
                    vverbose = true;
                    verbose = true;
                } else if (args[i].equals("-v")) {
                    verbose = true;
                } else if (args[i].equals("-h") || args[i].equals("--help")) {
                    exit = true;
                    info = true;
                    help = true;
                } else if (args[i].equals("--add-base")) {
                    withSourceBase = true;
                } else if (args[i].equals("--exclude-file-patterns")) {
                    excludeFilePatterns = args[++i];
                } else if (args[i].equals("--exclude-file-path-patterns")) {
                    excludeFilePathPatterns = args[++i];
                } else if (args[i].equals("--no-eol")) {
                    noEol = true;
                    if (noEol) { //move it around...
                        eol = "";
                    }
                } else if (args[i].equals("--unix-path")) {
                    unixPath = true;
                } else if (args[i].equals("--no-file-exist-check")) {
                    checkIfDependencyExistsOption = false;
                } else if (args[i].equals("--options")) {
                    String[] opts = args[++i].split(",");
                    for (String opt : opts) {
                        options.put(opt, "true");
                    }
                } else if (args[i].equals("-mm-mode")) {
                    perExtensions = false;
                } else if (args[i].equals("--chunk-extensions")) {
                    defaltWraps = Arrays.asList(args[++i].split(","));
                } else if (args[i].equals("--no-chunks")) {
                    defaltWraps = null;
                } else if (args[i].equals("--only-cp")) {
                    onlyClasspath = true;
                } else if (args[i].equals("--add-excluded-files")) {
                    excludedFilesString += args[i + 1] + " ";
                    String[] parts = args[++i].split(",");
                    excludedFiles.addAll(Arrays.asList(parts));
                } else if (args[i].equals("--file-search-excluded")) {
                    excludedDirsString += args[i + 1] + " ";
                    String[] parts = args[++i].split(",");
                    excludedListFiles.addAll(Arrays.asList(parts));
                } else if (args[i].equals("--create-output-dirs")) {
                    createDirsForOutput = true;
                }
//                else if (args[i].equals("--html-output")) {
//                    options.put("html-output", "true");
//                }
            }
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            exit = true;
        }

        if (help) {
            printUsage();
            return null;
        }
        
        //put defaults
        prefixPerExtension.put("", defaultPrefix);
        suffixPerExtension.put("", defaultSuffix + eol);

        if (!prefixPerExtension.containsKey("css")) {
            prefixPerExtension.put("css", "<link rel=\"stylesheet\" href=\"");
        }
        if (!prefixPerExtension.containsKey("js")) {
            prefixPerExtension.put("js", defaultPrefix);
        }

        if (!suffixPerExtension.containsKey("css")) {
            suffixPerExtension.put("css", "\">\n");
        }

        if (!suffixPerExtension.containsKey("js")) {
            suffixPerExtension.put("js", defaultSuffix + "\n"); //clean up defaults
        }

        //@todo review out validation
        //validate and refresh out
        if (cwd != null && output != null) {
            if (output.startsWith(cwd)) {
                output = output.substring(cwd.length());
                while (output.startsWith(CFile.separator)) {
                    output = output.substring(1);
                }
            }
        }

        boolean dotAdded = false;
        //sources preparation
        sourcesPaths = cleanPaths(cwd, sourcesPathsList);
        
        if (sourcesPaths.isEmpty()) {
          throw new FileNotFoundException(
                  "None of source paths provided exists! Stopping.");
        }
        
        //check if source base is specified, at leats one must be, check versus
        //first source base:
        FSFile srcFile = new CFile(cwd, sourcesPaths.get(0), true);
        if (sourceBase.isEmpty()) {
            if (srcFile.isFile()) {
                if (!dotAdded) {
                    //if its file, only current location makes default sense
                    sourceBase.add(".");
                }
                dotAdded = true;
            } else {
                //if its directory, pick it
                sourceBase.add(sourcesPaths.get(0));
            }
        }

        if (filesIncluded == null) {
            filesIncluded = "*";
        }

        if (linesToExclude == null) {
            linesToExclude = "/*D*/,//=,//:include,//:import,//:css";
        }

        if (filesToExclude == null) {
            filesToExclude = "////!ignore!////,/****!ignore!****/,##!ignore!##";
        }

        if (!verbose) {
            log.setLevel(LogLevel.NONE);
        }

        if (info) {
            String tmpPaths = "\n";
            for (String tmp : sourcesPaths) {
                tmpPaths += "\t" + new CFile(cwd, tmp, true).getPath() + "\n";
            }
            
            String configPath = "";
            String sep = "";
            for (int i = 0; i < args.length; i++) {
              String arg = args[i];
              if (arg != null && arg.equals("--config")) {
                if (i + 1 < args.length) {
                  configPath += sep + args[++i];
                }
                sep = ",";
              }
            }
            
            ps.println(
                " CompileJS config selected:");
            ps.println("  -i  Included file types: " + filesIncluded
                + "\n  -o  Output: "
                + (output == null ? "null"
                    : (new CFile(cwd, output, true)).getAbsolutePath() + ".EXT")
                + "\n  -s  Src dir: " + tmpPaths
                + "\n  -ir Ignoring RequireJS: " + (ignoreRJS ? "yes" : "no")
                + "\n  -nd No dependencies: " + (!dependencies)
                + "\n  -v  Verbosive: " + (verbose ? "yes" : "no")
                + "\n  -vv Very verbosive: " + (vverbose ? "yes" : "no")
                + "\n  -dl Excluding lines containing: " + linesToExclude
                + "\n  -dw Exclude blocks wrapped by: " + wrapsToExclude
                + "\n  -df Exclude files with keywords: " + filesToExclude
                + "\n  --parse-only-first-comment-dependencies: " + parseOnlyFirstComments
                + "\n  --source-base " + sourceBase
                + "\n  --keep-lines " + keepLines
                + "\n  --index: "
                + (generateIndex
                    ? " yes (Generate paths index only (no files merging).)"
                    : " no (Merge files.)")
                + "\n  --prefix (Index paths prefix): " + defaultPrefix
                + "\n  --suffix (Index paths suffix): " + defaultSuffix
                + "\n  --not-relative: " + (!relative ? "yes, paths will be absolute"
                    : "no, paths will be as defined in source base.")
                + "\n  --add-base: " + withSourceBase
                + "\n  --unix-path: " + unixPath
                + "\n  --cwd: " + (cwd == null ? "." : cwd)
                + "\n  --no-file-exist-check: " + !checkIfDependencyExistsOption
                + "\n  --config: " + configPath
                + "\n  --options: " + options
                + "\n  --add-excluded-files: " + excludedFilesString
                + "\n  --file-search-excluded: " + excludedDirsString
                + "\n\n");
        }

        if (output == null) {
            ps.println();
            ps.println(
                "***************************************************************");
            ps.println("* You must specify output (-o [file name]) path!\n"
                + "* Use --help or -h for more details.");
            ps.println("* Exiting.");
            ps.println(
                "***************************************************************");
            exit = true;
        }
                
        if (exit) {
            printArgs();
            //done = (System.nanoTime() - start);
            return null;
        }
        
        List<String> outputs = new ArrayList<>();

        if (output != null) {
            try {
                FSFile outputFile = new CFile(cwd, output, true);
                output = outputFile.getAbsolutePath();

                mainProcessor = new MainProcessor(log);

                if (vverbose) {
                    mainProcessor.setVeryVerbosive(true);
                }

                mainProcessor.setLineReaderCache(this.getLineReaderCache());
                mainProcessor.onlyClassPath(onlyClasspath);
                mainProcessor.setKeepLines(keepLines);

                if (!excludedListFiles.isEmpty()) {
                    mainProcessor.setExcludedFilesFromListing(
                        excludedListFiles.toArray(new String[]{}));
                }

                if (!excludedFiles.isEmpty()) {
                    mainProcessor.addFileNamesExcluded(
                        excludedFiles.toArray(new String[]{}));
                }

                mainProcessor.setNotCheckingIfFilesExist(!checkIfDependencyExistsOption);
                mainProcessor.setSourceBase(sourceBase.toArray(new String[0]));
                mainProcessor.setMergeOnly(filesIncluded.split(","));
                
                if (excludeFilePatterns != null) {
                    mainProcessor
                        .setFileExcludePatterns(excludeFilePatterns.split(","));
                }
                
                if (excludeFilePathPatterns != null) {
                    mainProcessor
                        .setFilePathExcludePatterns(excludeFilePathPatterns.split(","));
                }
                
                mainProcessor.setCwd(cwd);
                mainProcessor.setIgnoreRequire(ignoreRJS);
                mainProcessor.setLineIgnores(linesToExclude.split(","));
                mainProcessor.setStringsToIgnoreFile(filesToExclude.split(","));
                if (wrapsToExclude != null) {
                  mainProcessor.setFromToIgnore(wrapsToExclude.split(","));
                }

                if (parseOnlyFirstComments) {
                    mainProcessor.setCheckEveryLine(false);
                }

                Map<String, String> paths = mainProcessor
                    .getFilesListFromPaths(
                        sourcesPaths,
                        relative,
                        !dependencies,
                        output);

                logToConsole("Writing results...\n");

                if (createDirsForOutput) {
                  if (!outputFile.getParentFile().exists()) {
                    if (!outputFile.getParentFile().mkdirs()) {
                        throw new CouldNotCreateOutputDirException();
                    }
                  }
                }
                
                if (generateIndex) {
                    String result = MainProcessorHelper
                        .getPrefixScriptPathSuffixString(
                            paths,
                            prefixPerExtension,
                            suffixPerExtension,
                            withSourceBase,
                            unixPath
                        );

                    CFile writer = new CFile(output);

                    try {
                        logToConsole(result);
                        writer.saveString(result);
                        outputs.add(writer.getAbsolutePath());
                    } finally {
                    }
                } else {
                    if (perExtensions) {

                        String preTemplate = "    \"";//var template = [\n    \"",
                        String sufTemplate = "\"\n";//\\n\"\n].join('');\n",
                        String separator = "\\n\",\n    \"";//var template = [\n    \"",

                        mainProcessor.addProcessor(new JSTemplateProcessor(
                            preTemplate,
                            sufTemplate,
                            separator,
                            log
                        ));

                        mainProcessor.addProcessor(new JSStringProcessor(
                            "\"",
                            "\"",
                            "\\n",
                            log
                        ));

                        if (options.containsKey("wrap-js")) {
                            mainProcessor.addProcessor(new JSWrapperProcessor());
                        }

                        if (options.containsKey("injections")
                            || options.containsKey("line-injections")) {
                            InjectionProcessor p = new InjectionProcessor(
                                mainProcessor,
                                log
                            );
                            if (options.containsKey("line-injections")) {
                                p.setReplacingLine(true);
                            }
                            mainProcessor.addProcessor(p);
                        }

                        List<String> tmp = processPerExtensions(
                            paths,
                            mainProcessor,
                            output,
                            options,
                            defaltWraps);
                        
                        outputs.addAll(tmp);
                    } else {
                        mainProcessor.stripAndMergeFilesToFile(
                            paths, true, output);
                        outputs.add(new CFile(output).getAbsolutePath());
                    }
                }

                logToConsole("\n === Wrote results to file(s): " + output
                    + ".<extensions> === \n\n");

                if (info) {
                    ps.println(" === Merging/Index finished. ===\n");
                    ps.println(" === Heap: "
                        + Runtime.getRuntime().totalMemory() / 1024 / 1024
                        + "MB ===\n");
                }
            } finally {
                
                if (mainProcessor != null) {
                    mainProcessor.clearCache();
                }

                done = System.nanoTime() - start;
                
                if (info) {
                    String msg = " === Done in: "
                        + ((float) done / 1000000000.0)
                        + "s === \n";
                    ps.println(msg);
                }
            }
        }

        return outputs;
    }

    /**
     * 
     * @param paths
     * @param mainProcessor
     * @param out
     * @param options
     * @param wraps
     * @return
     * @throws IOException 
     */
    private List<String> processPerExtensions(
        Map<String, String> paths,
        MainProcessor mainProcessor,
        String out,
        Map<String, String> options,
        List<String> wraps)
        throws IOException {

        Map<String, String> other
            = new LinkedHashMap<String, String>();

        Map<String, Map<String, String>> extensionToNameMap
            = new LinkedHashMap<String, Map<String, String>>();

        //group files by extension
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String path = entry.getKey();
            String srcBase = entry.getValue();
            try {
                String ext = path.substring(path.lastIndexOf(".") + 1);
                if (!"".equals(ext)) {//check extension
                    //init
                    if (!extensionToNameMap.containsKey(ext)) {
                        extensionToNameMap.put(ext,
                            new LinkedHashMap<String, String>());
                    }
                    // collect ext => path:src-base
                    extensionToNameMap.get(ext).put(path, srcBase);
                } else {
                    //no extension: default collection
                    other.put(path, srcBase);
                }
            } catch (IndexOutOfBoundsException e) {
                other.put(path, srcBase);
            }
        }

        //all string chunks map
        Map<String, StringBuilder> allchunks
            = new HashMap<String, StringBuilder>();

        //are there any wraps defined? wraps are the wrapping codes that
        // define logical; chunks of code, example: *~css*
        boolean noWraps = (wraps == null);

        List<String> outputs = new ArrayList<String>();
        
        //process all files grouped by extension
        for (Map.Entry<String, Map<String, String>> entrySet : 
                extensionToNameMap.entrySet()) {
            Map<String, String> filePaths = entrySet.getValue();
            String ext = entrySet.getKey();
          
            String currentOut = out + "." + ext;
            if (noWraps) {

                //nothing to search for wraps - then just merge
                CFile writerFile = new CFile(currentOut);
                BufferedWriter writer = null;
                try {
                    writer = writerFile.getBufferedWriter(true);
                    mainProcessor.mergeFiles(filePaths, true, writer, currentOut);
                    outputs.add(writerFile.getAbsolutePath());
                } finally {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                }
            } else {
                // if there are wraps defined: split all files contents into 
                // wrapped blocks - per wrap definition 
                // chunks returned are mapped by extensions, not output, 
                // so example:
                // "": "defulaut output"
                // "htm": ".className {sdfgdasf} "
                // "htm": "<div/>"

                Map<String, StringBuilder> chunks
                    = mainProcessor.mergeFilesWithChunksAndStripFromWraps(
                        filePaths,
                        true,
                        currentOut,
                        wraps,
                        ext);
                mergeChunks(allchunks, chunks);
            }
        }

        //once wraps are extracted and grouped we can proceed some options
        // if html to js is applied, html wraps will be converted to javascript
        // code appending html to DOM.
        if (options.containsKey("html2js")) {
            String[] types = new String[]{"htm"};//used to be many types allowed
            for (String type : types) {
                StringBuilder html = allchunks.get(type);
                if (html != null) {
                    StringBuilder[] newJS = null;
                    if (!options.containsKey("html2js-multiline")) {
                        //returns two chunks to inject passed callback
                        newJS = turnHTMLToJS(html.toString().replace("\n", ""));
                    } else {
                        newJS = turnHTMLToJS(html.toString());
                    }
                    allchunks.remove(type);
                    StringBuilder js = allchunks.get("js");
                    if (js != null) {
                        allchunks.put("js", newJS[0]
                            .append("function(){\n")
                            .append(js)
                            .append("\n}")
                            .append(newJS[1]));
                    } else {
                        allchunks.put("js", newJS[0].append(newJS[1]));
                    }
                }
            }
        }
        //same option like in html case
        if (options.containsKey("css2js")) {
            StringBuilder css = allchunks.get("css");
            if (css != null) {
                StringBuilder[] newJS = null;
                if (!options.containsKey("css2js-multiline")) {
                    //returns two chunks to inject passed callback
                    newJS = turnCSSToJS(css.toString().replace("\n", ""));
                } else {
                    newJS = turnCSSToJS(css.toString());
                }
                allchunks.remove("css");
                StringBuilder js = allchunks.get("js");
                if (js != null) {
                    allchunks.put("js", newJS[0]
                        .append("function(){\n")
                        .append(js)
                        .append("\n}")
                        .append(newJS[1]));
                } else {
                    allchunks.put("js", newJS[0].append(newJS[1]));
                }
            }
        }

        // when wraps are applied, contents can be redirected and grouped
        // into files matching extension to wrap name, 3 are selected to be 
        // extracted:
        // js, htm, css outputs...
        if (!noWraps) {
            //if single html page option as output is applied, everything will
            //be put into one html "exe"
            if (options.containsKey("html-output")) {
                StringBuilder js = allchunks.get("js");
                StringBuilder css = allchunks.get("css");
                StringBuilder html = allchunks.get("htm");
                StringBuilder index = new StringBuilder();
//                index.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n");
//                index.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
//                index.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
                index.append("<html>\n");
                index.append("<head>\n");
                index.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />");
                index.append("<style>\n");
                index.append(css == null ? "" : css);
                index.append("\n</style>\n");
                index.append("</head>\n");
                index.append("<body>\n");
                index.append("<div class='templates'>\n");
                index.append(html == null ? "" : html);
                index.append("\n</div>\n");
                index.append("<script type=\"text/javascript\">\n//<![CDATA[\n");
                index.append(js == null ? "" : js);
                index.append("\n//]]>\n</script>\n");
                index.append("</body>\n");
                index.append("</html>");
                CFile output = new CFile(out + ".htm");//xhtml
                output.saveString(index.toString());
                outputs.add(output.getAbsolutePath());
                return outputs;
            } else {
                if (allchunks.isEmpty()) {
                    logToConsole("\n\n>>> No content to write. <<<\n\n\n");
                } else {
                    //...if many outputs: many outputs wil be written
                    outputs.addAll(
                        mainProcessor.writeOutputs(allchunks, out, true));
                }
            }
        }
        
        return outputs;
    }

    static String tpl1
        = "(function (callback) {\n"
        + "    var check = function () {\n"
        + "        var head = document.getElementsByTagName('head')[0];\n"
        + "        if (head) {\n"
        + "            var css = [\n";

    static String tpl2
        = "            ].join(\"\");\n"
        + "            var styleElement;\n"
        + "            styleElement = document.createElement('style');\n"
        + "            styleElement.setAttribute('type', 'text/css');\n"
        + "            if (styleElement.styleSheet) {\n"
        + "                styleElement.styleSheet.cssText = css;\n"
        + "            } else {\n"
        + "                styleElement.appendChild(document.createTextNode(css));\n"
        + "            }\n"
        + "            head.appendChild(styleElement);\n"
        + "            if (callback) {callback();}"
        + "        } else {\n"
        + "            setTimeout(check, 15);\n"
        + "        }\n"
        + "    };\n"
        + "    check();\n"
        + "}(";

    static String tpl3 = "));";

    static StringBuilder[] turnCSSToJS(String css) {
        String[] lines = css.split("\n");
        StringBuilder builder = new StringBuilder(tpl1);
        int i = 0;
        int size = lines.length;
        for (String line : lines) {
            line = line.replace("\\", "\\\\");
            line = line.replace("\"", "\\\"");
            builder.append("\t\"");
            builder.append(line);
            builder.append("\\n\"");
            i++;
            if (i < size) {
                builder.append(",\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append(tpl2);
        return new StringBuilder[]{builder, new StringBuilder(tpl3)};
    }

    static String htpl1
        = "(function (callback) {\n"
        + "    var check = function () {\n"
        + "        var body = document.getElementsByTagName('body')[0];\n"
        + "        if (body) {\n"
        + "            var html = [\n";

    static String htpl2
        = "            ].join(\"\");\n"
        + "            var div;\n"
        + "            div = document.createElement('div');\n"
        + "            div.setAttribute('class', 'html-to-js');\n"
        + "            div.innerHTML = html;\n"
        + "            body.appendChild(div);\n"
        + "            if (callback) {callback();}"
        + "        } else {\n"
        + "            setTimeout(check, 15);\n"
        + "        }\n"
        + "    };\n"
        + "    check();\n"
        + "}(";

    static String htpl3 = "));";

    static StringBuilder[] turnHTMLToJS(String html) {
        String[] lines = html.split("\n");
        StringBuilder builder = new StringBuilder(htpl1);
        int i = 0;
        int size = lines.length;
        for (String line : lines) {
            line = line.replace("\\", "\\\\");
            line = line.replace("\"", "\\\"");
            builder.append("\t\"");
            builder.append(line);
            builder.append("\\n\"");
            i++;
            if (i < size) {
                builder.append(",\n");
            } else {
                builder.append("\n");
            }
        }
        builder.append(htpl2);
        return new StringBuilder[]{builder, new StringBuilder(htpl3)};
    }

    /**
     * Function merges from one source chunks to another.
     *
     * @param to
     * @param from
     */
    static void mergeChunks(
        Map<String, StringBuilder> to,
        Map<String, StringBuilder> from) {
        for (Map.Entry<String, StringBuilder> entrySet : from.entrySet()) {
            StringBuilder fromS = entrySet.getValue();
            String key = entrySet.getKey();
            if (fromS != null) {
                key = chunkToExtension(key);
                StringBuilder toS = to.get(key);
                if (toS == null) {
                    toS = new StringBuilder("");
                    to.put(key, toS);
                }
                toS.append(fromS);
            }
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            this.setLineReaderCache(null); //release but not clear
        }
    }

    /**
     * @return the lineReaderCache
     */
    public Map<String, List<String>> getLineReaderCache() {
        return lineReaderCache;
    }

    /**
     * @param cache the lineReaderCache to set
     */
    public void setLineReaderCache(Map<String, List<String>> cache) {
        this.lineReaderCache = cache;
    }

    public static List<String> cleanPaths(
            String cwd,
            Iterable<String> sourcesPathsList) {
        List<String> sourcesPaths_ = new ArrayList<String>();
        for (String src : sourcesPathsList) {
            if (src.equals("")) {
                continue;
            }

            if (cwd != null) {
                if (src.startsWith(cwd)) {
                    src = src.substring(cwd.length());
                    while (src.startsWith(CFile.separator)) {
                        src = src.substring(1);
                    }
                }
            }

            if (src.trim().equals(".")) {
                src = "";
            }

            FSFile srcFile = new CFile(cwd, src, true);

            if (!srcFile.exists()) {
                ps.print("File: "
                    + srcFile.getAbsolutePath()
                    + "Does not exist. \nPlease check your configuration.");
            } else {
              sourcesPaths_.add(src);
            }
        }
        return sourcesPaths_;
    }

    private String[] validateArrayForNulls(String[] args) {
        int many = 0;
        for (String arg : args) {
            if (arg == null) many++;
        }
        
        if (many > 0) {
            String[] newArray = new String[args.length - many];
            int i = 0;
            for (String arg : args) {
                newArray[i] = arg;
                i++;
            }
            return newArray;
        } else {
            return args;
        }
    }
    //throws cwd being incorrect!
  private String[] addConfigFromConfigFiles(String[] args) throws IOException{
    String[] configPaths
            = getParamFromArgs(args, "--config", PROPERTY_FILE_NAME);
    cwd = getCwdFromArgs(args);
    ArrayList<String> tmp = new ArrayList<String>();
    List<String> argsList = Arrays.asList(args);
    tmp.addAll(argsList);
    
    for (String configPath : configPaths) {
      List<String> fromConfig = readConfig(configPath);
      if (fromConfig != null) {
        tmp.addAll(fromConfig);
      }
    }
    
    args = (String[]) tmp.toArray(new String[]{});
    
    //override cwd if any
    cwd = getCwdFromArgs(args);
    
    return args;
  }
}
