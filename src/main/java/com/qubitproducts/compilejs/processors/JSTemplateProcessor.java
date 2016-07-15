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


/**
 *
 * @author piotr
 */
public class JSTemplateProcessor extends JSStringProcessor {

    public static String JS_TEMPLATE_NAME = "js.template";
    
    public JSTemplateProcessor(String prefix, String suffix, String separator) {
        super(prefix, suffix, separator);
        this.jsTemplateName = JSTemplateProcessor.JS_TEMPLATE_NAME;
    }
}

