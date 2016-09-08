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

import java.util.logging.Logger;

/**
 *
 * @author piotr
 */
public class Log {
  /**
   * Log levels, Deafault and fine are {System.out.println} based. FINE is java
   * logger at FINE level.
   */
  static public enum LogLevel {
    CONSOLE,
    NONE,
    INFO,
    FINE,
    DEFAULT
  }
  
  private static final Logger LOGGER
      = Logger.getLogger(CompileJS.class.getName());
  
  public void setLevel(LogLevel level) {
    LOG_LEVEL = level;
    LOG = isLog();
  }
  
  private LogLevel LOG_LEVEL = LogLevel.INFO;
  
  public boolean LOG = isLog();
  
  public boolean isLog() {
    //return false;
    return LOG_LEVEL != LogLevel.NONE;
  }
  //@todo refactor this.
  public void log(String msg) {
    switch (LOG_LEVEL) {
      case CONSOLE:
        System.out.println(msg);
        break;
      case NONE:
        break;
      case INFO:
        LOGGER.info(msg);
        break;
      case FINE:
        LOGGER.fine(msg);
        break;
      default:
        LOGGER.info(msg);
    }
  }
    
  public void severe(String msg) {
    if (LOG_LEVEL != LogLevel.NONE) {
      LOGGER.severe(msg);
    }
  }
}
