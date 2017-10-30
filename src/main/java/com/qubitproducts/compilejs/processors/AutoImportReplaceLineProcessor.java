/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qubitproducts.compilejs.processors;

import java.util.List;

/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */
public class AutoImportReplaceLineProcessor implements SingleLineProcessor {

    @Override
    public String process(String line, String extension) {
        
        if (!extension.equals("js")) {
            return line;
        }
        
        if (line.startsWith("//:import")) {
            String newLine = line.substring(9).trim();
            int lastObjIdx = newLine.lastIndexOf(".");

            if (lastObjIdx != -1) {
                return "var "
                        + newLine.substring(lastObjIdx + 1) + " = "
                        + newLine + ";";
            } else {
                return "var " + newLine + " = " + newLine + ";";
            }
        }

        return line;
    }
}
