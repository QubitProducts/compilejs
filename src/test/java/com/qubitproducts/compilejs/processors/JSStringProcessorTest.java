/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.qubitproducts.compilejs.processors;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */
public class JSStringProcessorTest {
    
    public JSStringProcessorTest() {
    }

    /**
     * Test of prepareLine method, of class JSStringProcessor.
     */
    @Test
    public void testPrepareLine() {
        System.out.println("prepareLine");
        String line = 
"!{}][]!@#$%^&*()_+ąęłńćźż;\\n \\nf漢字\"\''|\\,.`~Ō∑ę®†ī¨^Ļąś∂ń©ķ∆Żłżźć√ļńĶ≤≥…ĺ÷„‚«azbdoitrjg";
        String expResult = 
"!{}][]!@#$%^&*()_+ąęłńćźż;\\\\n\\u2028\\\\nf漢字\\\"''|\\\\,.`~Ō\\u2211ę®\\u2020ī¨^Ļąś\\u2202ń©ķ\\u2206Żłżźć\\u221aļńĶ\\u2264\\u2265\\u2026ĺ÷\\u201e\\u201a«azbdoitrjg";
        String result = JSStringProcessor.prepareLine(line);
        assertEquals(expResult, result);
    }
    
}
