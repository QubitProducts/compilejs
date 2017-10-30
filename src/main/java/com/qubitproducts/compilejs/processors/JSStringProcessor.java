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
import static com.qubitproducts.compilejs.MainProcessorHelper.chunkToExtension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 *
 * @author peter.fronc@qubit.com
 */
public class JSStringProcessor implements Processor {

    public static String JS_TEMPLATE_NAME = "js.string";
    
    protected String jsTemplateName;
    
    String prefix;
    String suffix;
    String separator;

    private final static int JS_UNICODE_LENGTH = 4;
    private Log log;

    public JSStringProcessor(
            String prefix,
            String suffix,
            String separator,
            Log log) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.separator = separator;
        this.jsTemplateName = JSStringProcessor.JS_TEMPLATE_NAME;
        this.log = log;
    }

    @Override
    public void process(List<Object[]> chunks, String extension) {
        if (extension == null || !extension.equals("js")) {
            return;
        }
        for (Object[] chunk : chunks) {
            String key = (String) chunk[0];
            String skey = chunkToExtension(key);
            if (skey != null && skey.equals(this.jsTemplateName)) {
                try {
                    BufferedReader reader =
                        new BufferedReader(
                            new StringReader(
                                    ((StringBuilder)chunk[1]).toString()));
                    String line = reader.readLine();
                    StringBuilder builder = new StringBuilder(this.prefix);
                    while(line != null) {
                        line = prepareLine(line);
                        builder.append(line);
                        line = reader.readLine();
                        if (line != null){
                            builder.append(this.separator);
                        }
                    }
                    builder.append(this.suffix);
                    chunk[0] = "js";
                    chunk[1] = builder;
                } catch (IOException ex) {
                    log.log("IO Problem: " + ex.getMessage());
                }
            }
        }
    }

    public static String prepareLine(String line) {
        line = line.replace("\\", "\\\\");
        line = line.replace("\"", "\\\"");

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (    (ch < 32 && ch != '\t') || // from space below apart from tab
                    (ch > 512 && !Character.isLetterOrDigit(ch))) { //non printables

                String str = Integer.toHexString((int)ch);
                int strlen = str.length();

                if (strlen <= JS_UNICODE_LENGTH) {
                    while (str.length() < JS_UNICODE_LENGTH) {
                        str = "0" + str;
                    }
                    buf.append("\\u");
                    buf.append(str);
                }
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

}

