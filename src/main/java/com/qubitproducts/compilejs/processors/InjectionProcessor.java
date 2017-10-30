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
 *
 *  @author Peter (Piotr) Fronc 
 */
package com.qubitproducts.compilejs.processors;

import com.qubitproducts.compilejs.Log;
import com.qubitproducts.compilejs.fs.LineReader;
import static com.qubitproducts.compilejs.Utils.translateClasspathToPath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 *
 * @author peter.fronc@qubit.com
 */
public class InjectionProcessor implements Processor {

    String prefix = "";
    String suffix = "";
    String INJECT_STR = "//:inject";

    private boolean replacingLine = false;
    private Log log;
    private String cwd = null;
    private String[] srcBase = null;
    private Map<String, List<String>> lineReaderCache;
    private String extension;

    public InjectionProcessor(Log log) {
        this.log = log;
    }

    public InjectionProcessor(
            String cwd,
            String[] srcBase,
            Map<String, List<String>> lineReaderCache,
            Log log) {
        this(log);
        this.cwd = cwd;
        this.srcBase = srcBase;
        this.lineReaderCache = lineReaderCache;
        this.extension = "js";
    }

    public InjectionProcessor(
            String prefix,
            String suffix) {
        super();
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void process(List<Object[]> chunks, String extension) {
        for (Object[] chunk : chunks) {
//            String key = (String) chunk[0];
//            String skey = chunkToExtension(key);
//            if (skey != null && skey.equals(JS_TEMPLATE_NAME)) {
            try {
                BufferedReader reader
                    = new BufferedReader(
                        new StringReader((
                                (StringBuilder) chunk[1]
                        ).toString()));

                String line = reader.readLine();
                StringBuilder builder = new StringBuilder(this.prefix);
                while (line != null) {
                    this.processSingleLine(builder, line);
                    line = reader.readLine();
                    if (line != null) {
                        builder.append("\n");
                    }
                }

                builder.append(this.suffix);

                chunk[0] = extension;
                chunk[1] = builder;
            } catch (IOException ex) {
                log.log("IO Problem: " + ex.getMessage());
            }
//            }
        }
    }

    /**
     * @return the replacingLine
     */
    public boolean isReplacingLine() {
        return replacingLine;
    }

    /**
     * @param replaceLine the replacingLine to set
     */
    public void setReplacingLine(boolean replaceLine) {
        this.replacingLine = replaceLine;
    }

    private void processSingleLine(
            StringBuilder builder,
            String line)
            throws FileNotFoundException, IOException {

        boolean skip = false;

        if (line.contains(INJECT_STR)) {
            int injectStart = line.indexOf(INJECT_STR);
            String formula = line.substring(injectStart);
            String[] parts = formula.split(" ");
            if (parts.length > 1) {

                //pick the path
                int j = 1;

                String path
                        = translateClasspathToPath(parts[j])
                        + "." + extension;

//                while (path == null || path.trim().equals("")) {
//                    path = parts[++j];
//                }

                //check the path
                File f = new File(path);
                boolean exists = false;
                if (this.srcBase != null) {
                    for (String str : this.srcBase) {
                        File tmp = new File(cwd, str);
                        tmp = new File(tmp, path);
                        if (tmp.exists()) {
                            f = tmp;
                            exists = true;
                            break;
                        }
                    }
                }

                // process file if exists
                if (exists || f.exists()) {
                    skip = true;
                    if (!this.isReplacingLine()) {
                        String pre = line.substring(0, injectStart);
                        builder.append("\n");
                        builder.append(pre);
                    }

                    LineReader lr = new LineReader(f, this.lineReaderCache);
                    String tmp;
                    while ((tmp = lr.readLine()) != null) {
                        builder.append(tmp);
                        builder.append("\n");
                    }

                    if (!this.isReplacingLine()) {
                        //bring suffixed stuff...
                        for (int i = j + 1; i < parts.length; i++) {
                            builder.append(" ");
                            builder.append(parts[i]);
                        }
                    }
                }
            }
        }

        if (!skip) {
            builder.append(line);
        }
    }
}
