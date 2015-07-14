/*
 /*
 *  Copyright @ QubitProducts.com
 *
 *  CompileJS is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MiniMerge is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License.
 *  If not, see LGPL licence at http://www.gnu.org/licenses/lgpl-3.0.html.
 *
 *  @author Peter (Piotr) Fronc 
 */

package com.qubitproducts.compilejs;

import com.qubitproducts.compilejs.fs.FSFile;
import com.qubitproducts.compilejs.fs.LineReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */


public class MainProcessorHelperTest {
    
    public MainProcessorHelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of stripFileFromWraps method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFileFromWraps() throws Exception {
        System.out.println("stripFileFromWraps");
        FSFile file = null;
        String[] wraps = null;
        String replacement = "";
        MainProcessorHelper.stripFileFromWraps(file, wraps, replacement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFromWrap method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWrap_4args_1() throws Exception {
        System.out.println("stripFromWrap");
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String wrap = "";
        String replacement = "";
        MainProcessorHelper.stripFromWrap(reader, writer, wrap, replacement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of chunkToExtension method, of class MainProcessorHelper.
     */
    @Test
    public void testChunkToExtension() {
        System.out.println("chunkToExtension");
        String in = "";
        String expResult = "";
        String result = MainProcessorHelper.chunkToExtension(in);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStringInChunks method, of class MainProcessorHelper.
     */
    @Test
    public void testGetStringInChunks_3args() {
        System.out.println("getStringInChunks");
        List<String> lines = null;
        List<String> wraps = null;
        String defaultChunkName = "";
        List expResult = null;
        List result = MainProcessorHelper.getStringInChunks(lines, wraps, defaultChunkName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStringInChunks method, of class MainProcessorHelper.
     */
    @Test
    public void testGetStringInChunks_4args() {
        System.out.println("getStringInChunks");
        List<String> lines = new ArrayList<>();
        lines.add("/*xxx*/12345678/*~xxx*/");
        lines.add("/*y*/xyz");
        lines.add("   /*xx*/12345678/*~xx*/");
        lines.add("/*~y*/");
        List<String> wraps = new ArrayList<>();
        wraps.add("/*~xxx*/");
        wraps.add("/*~y*/");
        String defaultChunkName = "*";
        boolean fromWrapChar = true;
        
        List<Object[]> result = 
            MainProcessorHelper
                .getStringInChunks(lines, wraps, 
                    defaultChunkName, fromWrapChar);
        
        assertEquals(
            "12345678",
            (StringBuilder)(result.get(0)[1]) + "");
        
        assertEquals(
            "xyz\n" +
            "   /*xx*/12345678/*~xx*/\n",
            (StringBuilder)(result.get(2)[1]) + "");
    }

    /**
     * Test of replaceFirstChar method, of class MainProcessorHelper.
     */
    @Test
    public void testReplaceFirstChar() {
        System.out.println("replaceFirstChar");
        String string = "123456781234565768";
        char ch = '4';
        String with = "x";
        String expResult = "123x56781234565768";
        String result = MainProcessorHelper.replaceFirstChar(string, ch, with);
        assertEquals(expResult, result);
    }

    /**
     * Test of stripFromWrap method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWrap_3args() throws Exception {
        System.out.println("stripFromWrap");
        List<String> lines = null;
        String wrap = "";
        String replacement = "";
        List<String> expResult = null;
        List<String> result = MainProcessorHelper.stripFromWrap(lines, wrap, replacement);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFromWrap method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWrap_4args_2() throws Exception {
        System.out.println("stripFromWrap");
        LineReader reader = null;
        BufferedWriter writer = null;
        String wrap = "";
        String replacement = "";
        MainProcessorHelper.stripFromWrap(reader, writer, wrap, replacement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFromWraps method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWraps_3args_1() throws Exception {
        System.out.println("stripFromWraps");
        List<String> lines = null;
        List<String> wraps = null;
        String replacement = "";
        List<String> expResult = null;
        List<String> result = MainProcessorHelper.stripFromWraps(lines, wraps, replacement);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFromWraps method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWraps_3args_2() throws Exception {
        System.out.println("stripFromWraps");
        List<String> lines = null;
        String[] wraps = null;
        String replacement = "";
        List<String> expResult = null;
        List<String> result = MainProcessorHelper.stripFromWraps(lines, wraps, replacement);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFromWraps method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFromWraps_4args() throws Exception {
        System.out.println("stripFromWraps");
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String[] wraps = null;
        String replacement = "";
        MainProcessorHelper.stripFromWraps(reader, writer, wraps, replacement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyTo method, of class MainProcessorHelper.
     */
    @Test
    public void testCopyTo() throws Exception {
        System.out.println("copyTo");
        FSFile from = null;
        FSFile to = null;
        MainProcessorHelper.copyTo(from, to);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFileFromWrap method, of class MainProcessorHelper.
     */
    @Test
    public void testStripFileFromWrap() throws Exception {
        System.out.println("stripFileFromWrap");
        FSFile file = null;
        String wrap = "";
        String replacement = "";
        MainProcessorHelper.stripFileFromWrap(file, wrap, replacement);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPrefixScriptPathSuffixString method, of class MainProcessorHelper.
     */
    @Test
    public void testGetPrefixScriptPathSuffixString_5args_1() {
        System.out.println("getPrefixScriptPathSuffixString");
        Map<String, String> paths = null;
        String prefix = "";
        String suffix = "";
        boolean appendSrcBase = false;
        boolean unixStyle = false;
        String expResult = "";
        String result = MainProcessorHelper.getPrefixScriptPathSuffixString(paths, prefix, suffix, appendSrcBase, unixStyle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPrefixScriptPathSuffixString method, of class MainProcessorHelper.
     */
    @Test
    public void testGetPrefixScriptPathSuffixString_5args_2() {
        System.out.println("getPrefixScriptPathSuffixString");
        Map<String, String> paths = null;
        Map<String, String> prefixes = null;
        Map<String, String> suffixes = null;
        boolean appendSrcBase = false;
        boolean unixStyle = false;
        String expResult = "";
        String result = MainProcessorHelper.getPrefixScriptPathSuffixString(paths, prefixes, suffixes, appendSrcBase, unixStyle);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
