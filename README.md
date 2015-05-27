# CompileJS - Vanilla alternative to AMD & CommonJS

================================================================================

## Summary

CompileJS allows easy javascript merging with its dependencies in a defined order.
It is lightweight and written purely in java to be platform independant.

CompileJS was forked from MiniMerge https://github.com/QubitProducts/compilejs

CompileJs was created with classpath paradigm in mind.

================================================================================

## Most important features

Directives included in files.

### Traditional Include directive

```
    //:include file/path/FileName.js
    //:include file/path/FileName.css
```

Any `include` directive specifies path to dependency of the file containing it.
If file pointed by directive exists it will be queued before the file containing
it - unless is already inthe queue. Most generic dependency managment similiar to
famous sprockets or require in nodejs.

### Import Classpath directive

```
//:import org.package.name.Object
```

Import directive differ from include directives with input parameter type, which
is classpath instead of file path. Classpath use more secure and clean way to 
reference dependencies. 

Following import directive:

```
    //:import org.package.Object
```

will indirectly translate to:

```
    //:include org/package/Object.js
```

### CSS injection classpath directive
```
    //:css org.package.name.Object
```

CSS directives are created to support CSS file types injection. The directive
above will translate to:

```
    //:include org/package/name/Object.css
```

### Content injection:

```
    //:inject com.package.name.Inject#json
```

CompileJS supports direct content injection. Directive above will cause injecting

com/package/name/Inject.json file from first available file found in source base
paths.

### Source Base Paths

CompileJS was created with thoughts of multiple source bases used while developing
Javascript application.

To specify more than one path where sources are stored use `-cp` or `--source-base`
parameter. Those arguments can be specified more than once.

### Output

By default CompileJS outputs all files defined by dependency directives to separate
locations, grouped by file extension names. See examples for more information.


### Inline Injections - Tags

CompileJS supports addons such as inline CSS asnd HTML injections. Javascript files
can contain pure HTML and CSS fragments that will be merged to their output locations.

Example how to input HTML fragment that will be added to DOM:

```javascript
    /*htm*
    <h1 class="eye-pain">
        This Eye Pain Fragment
    </h1>
    <div>
        Will be added to DOM.
    </div>
    *~htm*/
```

Similar way CSS fragments can be added:

```css
    /*css*
        .eye-pain {
            color: red;
            background: blue;
        }
    *~css*/
```

================================================================================

Details

Program will merge or list files contents in specific order if dependencies
keywords are used, for example lines in a file named myFile.css:

``` 
     //:include css/style.css
     //:include ../otherdir/license.txt
```

will make program to order merged files contents as paths below: 

```
    [srcBase]/css/style.css
    [scrBase]/../otherdir/license.txt
    [srcBase]myFile.css
```

[srcBase] is runtime --src-base argument, by default, it is a current
 directory value. Dependency detection works recursively.
Program supports basic sprockets style dependencies addressing for JS files dependencies, 

```
     //=require path
```

will be translated to:

```
     //:include path.js
```

(Notice how its translated! Only direct paths are supported).

Program also can filter contents by:

- excluding line of text (keyword, for example: //delete )

- excluding block of text (by keyword, like: -dw /*~keyword*/ etc.). For example, using .~keyword. will 
filter following text:

```
          AAA
          //any foo                            .keyword.
          BBB
          .~keyword.   /// or bar
          CCC
```

to following (notice 3 mniddle lines excluded):

```
    AAA


    CCC
```

- excluding entire file (keyword, for example: -df /**exclude this file**/ will cause any file to be excluded if contains  /**exclude this file**/ anywhere in its contents) 

Content filtering is applied to final merged output file only (after merging 
files set).

Program can simply list files instead of merging its contents by using --index 
option - in addition, you can use prefix and suffix for each file path in the
list to be prefixed/suffixed. Files list is same ordered like in merge process.

When using CompileJS it is strongly recommended to specify source base and the
file where process starts from. Please see usage list for more details - compilejs supports MULTIPLE source bases.
================================================================================

##Official Usage Page                                                               

```                                                                      
  -i <include extensions - file ENDINGS, default: * (all)>            
      example: -i .js,.css,.xml (default: .js)                        
  -o <output file path> This argument must be specified.              
  --info Show final config summary(info)                              
  -s <src dir/file path> if it is not directory, --source-base mode is
     enabled. If it is directory, program will take as subject all  
     files from that directory and will treat it as a source base.    
  -ir ignore Require.js deps (default: false)                         
  --index It will ignore merging and generate prefix,suffix list      
  --prefix <prefix for index generation>                              
  --source-base comma separated source bases, if specified, all       
                dependencies will be searched with an order defined with
                this parameter.  Example: "src, libs/src"           
  --suffix <suffix for index generation>                              
  --not-relative <absolute paths index generation, default: false>    
  -vv very verbose                                                    
  -v verbose                                                          
  -nd <process no dependencies in files? see: //= and //:include>     
  -dl <cut lines containing strings(comma separated)>                    
   example: /*D*/ or /*X*/ (defaults: /*D*/,//=,//:include,//= require)
  -df <file exclude patterns, defaults:                               
   /****!ignore!****/,////!ignore!////,##!ignore!## (comma separated) 
  -dw <wrapped text cut by strings(comma separated)                   
   example: /*start*/ <cut text> /*~start*/ in file, command line arg 
   will be: -dw /*~start*/ (keep ~ unique, it's used to mark endings. 
 --parse-only-first-comment-dependencies for performance reasons      
   you may want to parse dependencies contents only for first lines   
   starting in a file as a comment (it means that program will     
   not go through file contents to analyse deps and only till         
   comment like contents is present)                                  
 --add-base If this option is added and --index is used the file list 
   index will have source base appended accordigly to where it is found.
 --exclude-file-patterns If this option is specified, each comma       
   separated string will be tested with java regex method to match 
   name of file. If any of strings match - file will be     
   excluded from processing.
 --keep-lines If passed, stripping process will put empty lines in 
              place of cut out ones. Default: false.
 --unix-path If index listings should care for UNIX style output,          default is: true
 --exclude-file-path-patterns If this option is specified, each comma 
   separated string will be tested with java regex method to match 
   name of full file path. If any of strings match - file will be     
   excluded from processing.
 --no-eol If set, and --index option is selected, no end of line will be 
          added to the index list items.
 --cwd Specify current working directory. Default is current directory.
       It does not affect -o property. Use it when you cannot manage CWD.
 --no-file-exist-check if added, MM will NOT check if dependencies EXIST. 
                   It also assumes that first class path is used ONLY -                    first entry from --source-base will be used ONLY.
 --chunk-extensions array, comma separated custom extensions used for wraps.
 --only-cp Pass to make compilejs use only classpath baesed imports.
   Default: *~css*,*~htm*,*~js.template*  Those wrap definitions are used to take out
   chunks of file outside to output with extension defined by wrap keyword.
   For example: /*~c-wrap*/ chunk will be written to default OUTPUT 
   (-o option) plus c-wrap extension. Its advised to use alphanumeric
 characters and dash and underscore and dot for custom wraps.
 --options compilejs specific options: 
          css2js -> convert css output to javascript. The javascript
                   producing CSS will be inserted before all JS code.
          css2js-multiline -> Display js from css in mulitlines.
          wrap-js -> If javascript files should be wrapped in functions.
          html-output -> all files should be merged into one html
                         output file.

 --add-excluded-files   if some files must be absolutely excluded 
                      list them comma separated.                  
 --file-search-excluded Do not enter to directories (when directory 
          specified).
          Pass java regex inside: _dname.* will cause any  
          directory name starting with _dname to be ignored.
          Note: this option does not apply for dependencies search.
 --help,-h Shows this text                                        
 --config [filename] Default file name is compilejs.properties 
================================================================================


For even more examples, run java -jar compilejs.jar -h
```

##More Examples

  Merging all javascript from "src" directory and analysing dependencies:


    java -jar compilejs.jar -s src -o output -i .js
    
  
  Extensions to "output" will be added automatically.
  Command will cause fetching all files from src directory recursively.
  If any path in files is defined as dependency:

```
    //:include my/file.js
```

  Then it is expected to be in my/file.js location, by default source base is a current execution location.
  To change source base, add --source-base parameter, multiple values comma separated are allowed:

```
    java -jar compilejs.jar -s src -o output -i .js --source-base "src,other"
```

  Now, the dependency is expected to be in src/my/file.js or other/my/file.js
  location. -i option defines matched string(s) at the end of file name (multiple options allowed, comma separated)

  To list files only instead of merging their contents, add --index option.

```
    java -jar compilejs.jar -s src -o output -i .js --source-base "src,other" --index
```

or

```
    java -jar compilejs.jar -s src -o output -i .js -cp src,other --index
```

  To see the output in console and also other useful information use -v (verbosive) or -vv (very verbosive) option.

  To add prefix and suffix to listed index, add --prefix and --suffix arguments, like below:

```
    java -jar compilejs.jar -s src -o output -i .js --source-base "src,other" --index --prefix "<script src='" --suffix "'></script>"
```

  During merging files process its very useful to "cut" out some of its contents, like
debugging blocks, testing code etc., compilejs has 3 levels of content filtering, single line level,
block of lines and entire files. Please see, -dl, -dw and -df options.

  -dl is used to delete lines containing one of comma separated values passed to -dl, for example:
    -dl "console.log,console.debug" Will cause removing all lines from sources that contain console.log or console.debug strings.

  -dw is used to exclude entire blocks, it requires special format, for example:
    -dw "/~match/" will cause blocks starting with /match/ and ending with /~match/
    to be excluded from merge process. -dw, similary to -dl, accepts multiple entries.

  -df is used to excluded files. Any comma separated value that is contained by file will cause to exclude that file from merge process.
  This option applies also to --index option (unlike to -dl and -dw).

  To make compilejs stop analyzing dependencies use -nd option.

  To change current working directory for compilejs, use --cwd option.

  compilejs by default use relative paths, to make it using absolute paths use 
  --not-relative option



