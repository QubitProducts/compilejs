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

import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.IOException;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;


/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */
public class Watcher {
    
    public static void main(String[] args) 
        throws IOException, InterruptedException{
        new Watcher().watch();
    }
    
    
    public void watch()
        throws
        IOException,
        InterruptedException {

        final Path myDir = Paths.get("xxxxxxxxxx");
        try {
            final WatchService watcher = 
                myDir.getFileSystem().newWatchService();
            
            final Kind[] kinds = new WatchEvent.Kind[]{
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.OVERFLOW
            };
            
            myDir.register(watcher, kinds, SensitivityWatchEventModifier.HIGH);
            
            registerTree(myDir, watcher, kinds);
            
            System.out.println("Attached");
            
            while(true) {
                // Obtaining watch keys
                final WatchKey key = watcher.take();
                //if (key == null) continue;
                // key value can be null if no event was triggered
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    final Kind<?> kind = watchEvent.kind();
                    // Overflow event
                    if (StandardWatchEventKinds.OVERFLOW == kind) {
                        continue; // loop
                    } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path tmp = ((Path)watchEvent.context());
//                        final Path child = myDir.resolve(tmp);
                        System.out.println("Created " + tmp);
                        registerTree(myDir, watcher, kinds);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("Deleted " + watchEvent.context());
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("Modified " + watchEvent.context());
                    }
                }
                
                if(!key.reset()) {
					break;
				}
            }
        } finally {
        }
        
    }

    private void registerTree(
        Path myDir,
        final WatchService watcher,
        final Kind[] kinds) 
        throws IOException {
        
        Files.walkFileTree(myDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(
                    Path dir,
                    BasicFileAttributes attrs)
                    
                    throws IOException {
                    dir.register(watcher, kinds, SensitivityWatchEventModifier.HIGH);
                        return FileVisitResult.CONTINUE;
                }
            });
    }
}
