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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */
public class Watcher {

    public static void main(String[] args)
        throws IOException, InterruptedException {
                
        new Watcher().watch(
            System.getProperty("user.home") + "/xxx",
            null,null, null);
    }
    
    private Thread thread;
    private volatile boolean watching = false;

    public void watchNow(String path, 
        Callback changeCallback,
        Callback eachFileCallback,
        List<String> excludes)
        throws
        IOException,
        InterruptedException {

        try {
            this.watching = true;
            
            final Kind[] kinds = new WatchEvent.Kind[]{
                StandardWatchEventKinds.OVERFLOW,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            };

            final Path myDir;
            final WatchService watcher;
            
            if (Paths.get(path).toFile().isDirectory()) {
                
                myDir = Paths.get(path);
                watcher = myDir.getFileSystem().newWatchService();
                
                myDir.register(watcher, kinds, 
                    SensitivityWatchEventModifier.HIGH);

                registerTree(myDir, watcher, kinds);
            } else {
                myDir = Paths.get(path).getParent();
                watcher = myDir.getFileSystem().newWatchService();
                myDir.register(watcher, kinds,
                    SensitivityWatchEventModifier.HIGH);
            }
            
            System.out.println("Watching attached to " + myDir);

            while (this.watching) {
                
                final WatchKey key = watcher.take();
                //if (key == null) continue; //poll case
                // key value can be null if no event was triggered
                boolean called = false;
                for (WatchEvent<?> event : key.pollEvents()) {
                    final Kind<?> kind = event.kind();
                    // Overflow event
                    if (StandardWatchEventKinds.OVERFLOW == kind) {
                        continue; // loop
                    } else {
                        Path tmpPath = (Path) key.watchable();
                        tmpPath = tmpPath.resolve(((Path) event.context()));
                        if (kind == ENTRY_CREATE) {
                            System.out.println("Created " + tmpPath.toAbsolutePath());
                            registerTree(tmpPath, watcher, kinds);
                        } else if (kind == ENTRY_DELETE) {
                            System.out.println("Deleted " + event.context());
                        } else if (kind == ENTRY_MODIFY) {
                            System.out.println("Modified " + event.context());
                        }
                        
                        //check if not in excludes
                        boolean inExcluded = false;
                        if (excludes != null) {
                            File tmp = tmpPath.toFile().getCanonicalFile();
                            for (String exclude : excludes) {
                                if (tmp.equals(new File(exclude)
                                        .getCanonicalFile())) {
                                    inExcluded = true;
                                }
                            }
                        }
                        
                        //not excluded - process
                        if (!inExcluded) {
                            if (eachFileCallback != null) {
                                Path dir = (Path) key.watchable();
                                dir = dir.resolve(((Path) event.context()));
                                eachFileCallback.call(dir);
                            }
                            called = true;
                        }
                    }
                }
                
                if (called) {
                    if (changeCallback != null) {
                        System.out.println("Unignored changes detected.");
                        changeCallback.call(key);
                    }
                }
                
                if (!key.reset()) {
                    break;
                }
            }
        } catch (java.nio.file.NotDirectoryException ex) {
            System.out.println("Cannot watch plain file.");
        } finally {
            watching =  false;
        }

    }
    
    int maxErrorMsgs = 99;
    volatile int maxErrorCount = 0;
    
    private void registerTree(
        Path myDir,
        final WatchService watcher,
        final Kind[] kinds)
        throws IOException {

        Files.walkFileTree(myDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                final Path dir,
                BasicFileAttributes attrs)
                throws IOException {
                if (!Files.isHidden(dir)) {
                    
                    try{
                        dir.register(watcher, kinds, SensitivityWatchEventModifier.HIGH);
                    } catch (Exception ex) {
                        if (maxErrorCount++ < maxErrorMsgs)
                        System.out.println("Cannot register watch at file " + 
                            dir + "\n. Exception:" + ex.getMessage());
                        if (maxErrorCount == maxErrorMsgs) {
                            System.out.println("Stopping errors log...");
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void watch(final String path, 
        final Callback changeCallback,
        final Callback eachFileCallback,
        final List<String> excludes) {
        
        if (this.thread != null) {
            System.out.println("Already watching.");
        }
                
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Watcher.this
                        .watchNow(path, changeCallback, eachFileCallback, excludes);
                } catch (IOException ex) {
                    Logger.getLogger(Watcher.class.getName())
                            .log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Watcher.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
        
        this.thread.start();
    }

    public void stop() {
        watching = false;
    }
}
