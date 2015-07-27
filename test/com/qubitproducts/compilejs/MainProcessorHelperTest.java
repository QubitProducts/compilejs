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
            "   /*xx*/12345678/*~xx*/",
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
    
}
