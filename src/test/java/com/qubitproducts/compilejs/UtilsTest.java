/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qubitproducts.compilejs;

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
public class UtilsTest {
    
    public UtilsTest() {
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
     * Test of isClasspath method, of class Utils.
     */
    @Test
    public void testIsClasspath() {
        System.out.println("isClasspath");
        String string = "ab.cd._efgh.ijk";
        boolean expResult = false;
        boolean result = Utils.isClasspath(string);
        assertEquals(expResult, result);
        string = "ab.cd.a_efgh.ijk";
        expResult = true;
        result = Utils.isClasspath(string);
        assertEquals(expResult, result);
        string = "ab.cd.1a_efgh.ijk";
        expResult = false;
        result = Utils.isClasspath(string);
        assertEquals(expResult, result);
    }

    /**
     * Test of translateClasspathToPath method, of class Utils.
     */
    @Test
    public void testTranslateClasspathToPath() {
        System.out.println("translateClasspathToPath");
        String path = "a.b.c.d.s";
        String expResult = "a/b/c/d/s";
        String result = Utils.translateClasspathToPath(path);
        assertEquals(expResult, result);
    }
}
