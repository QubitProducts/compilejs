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
package com.qubitproducts.compilejs.fs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Peter Fronc <peter.fronc@qubitdigital.com>
 */
public class CFile implements FSFile {

    static public final char separatorChar = File.separatorChar;
    static public final String separator = File.separator;

    /**
     * @return the cache
     */
    public Map<String, String> getCache() {
        return cache;
    }

    /**
     * @param aCache the cache to set
     */
    public void setCache(Map<String, String> aCache) {
        cache = aCache;
    }

    private File plainFile = null;

    public File getFile() {
        return getPlainFile();
    }

    private Map<String, String> cache = null;

    @Override
    public void clear() {
        if (getCache() != null) {
            String canonical;
            try {
                canonical = this.getCanonicalPath();
                getCache().remove(canonical);
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Reads file as a string - not using cache.
     *
     * @return
     * @throws IOException
     * @throws IsDirectoryException
     */
    @Override
    public String getAsString()
        throws IOException, IsDirectoryException {
        return getAsString(false);
    }

    /**
     * If cache should be used. Note that cache object must be set for cache to
     * be used, see `setCache` for more details..
     *
     * @param useCached
     * @return
     * @throws IOException
     * @throws IsDirectoryException
     */
    @Override
    public String getAsString(boolean useCached)
        throws IOException, IsDirectoryException {
        String cachedFound = null;
        String canonical = null;

        if (getCache() != null) {
            canonical = getPlainFile().getCanonicalPath();
            if (useCached) {
                cachedFound = getCache().get(canonical);
            } else {
                getCache().remove(canonical);
            }
        }

        if (cachedFound == null) {
            if (this.isDirectory()) {
                throw new IsDirectoryException();
            }

            try (BufferedReader reader = new BufferedReader(
                new FileReader(getPlainFile()))) {
                StringBuilder builder = new StringBuilder();
                CharBuffer charBuffer = CharBuffer.allocate(1024);
                while ((reader.read(charBuffer)) != -1) {
                    charBuffer.flip();
                    builder.append(charBuffer);
                }
                String result = builder.toString();
                if (getCache() != null) {
                    getCache().put(canonical, result);
                }
                return result;
            }
        } else {
            return cachedFound;
        }
    }

    public CFile(String pathname) {
        plainFile = new File(pathname);
    }

    public CFile(File file) {
        plainFile = file;
    }

    public CFile(FSFile file) {
        this(file.getPath());
    }

    public CFile(String parent, String child) {
        plainFile = new File(parent, child);
    }

    public CFile(String parent, String child, boolean validate) {
        if (validate && new File(child).isAbsolute()) {
            plainFile = new File(child);
        } else {
            plainFile = new File(parent, child);
        }
    }

    public CFile(File parent, String child) {
        plainFile = new File(parent, child);
    }

    public CFile(FSFile parent, String child) {
        this(parent.getPath(), child);
    }

    public CFile(URI uri) {
        plainFile = new File(uri);
    }

    @Override
    public String getName() {
        return getPlainFile().getName();
    }

    @Override
    public String getParent() {
        return getPlainFile().getParent();
    }

    @Override
    public CFile getParentFile() {
        return new CFile(getPlainFile().getParentFile());
    }

    @Override
    public String getPath() {
        return getPlainFile().getPath();
    }

    @Override
    public boolean isAbsolute() {
        return getPlainFile().isAbsolute();
    }

    @Override
    public String getAbsolutePath() {
        return getPlainFile().getAbsolutePath();
    }

    @Override
    public CFile getAbsoluteFile() {
        return new CFile(getPlainFile().getAbsoluteFile());
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return getPlainFile().getCanonicalPath();
    }

    @Override
    public CFile getCanonicalFile() throws IOException {
        return new CFile(getPlainFile().getCanonicalFile());
    }

    @Override
    public boolean canRead() {
        return getPlainFile().canRead();
    }

    @Override
    public boolean canWrite() {
        return getPlainFile().canWrite();
    }

    @Override
    public boolean exists() {
        return getPlainFile().exists();
    }

    @Override
    public boolean isDirectory() {
        return getPlainFile().isDirectory();
    }

    @Override
    public boolean isFile() {
        return getPlainFile().isFile();
    }

    @Override
    public boolean isHidden() {
        return getPlainFile().isHidden();
    }

    @Override
    public long lastModified() {
        return getPlainFile().lastModified();
    }

    @Override
    public long length() {
        return getPlainFile().length();
    }

    @Override
    public boolean createNewFile() throws IOException {
        return getPlainFile().createNewFile();
    }

    @Override
    public boolean delete() {
        return getPlainFile().delete();
    }

    class Error {
        boolean value = false;
    }
    
    @Override
    public boolean delete(boolean recursive) throws IOException {
        final Error error = new Error();
        Path path = Paths.get(this.plainFile.getAbsolutePath());
        
        if (recursive) {
            SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(
                    Path file,
                    BasicFileAttributes attrs)
                    throws IOException {
                    
                    Files.delete(file);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(
                    Path dir,
                    IOException exc) throws IOException {

                    if (exc != null) {
                        error.value = true;
                    }
                    
                    Files.delete(dir);
                    return CONTINUE;
                }
            };
            
            Files.walkFileTree(path, visitor);
        } else {
            Files.delete(path);
        }
        
        return !error.value;
    }

    @Override
    public void deleteOnExit() {
        getPlainFile().deleteOnExit();
    }

    @Override
    public String[] list() {
        return getPlainFile().list();
    }

    @Override
    public String[] list(FilenameFilter filter) {
        return getPlainFile().list(filter);
    }

    @Override
    public FSFile[] listFiles() {
        File[] files = getPlainFile().listFiles();
        if (files == null) {
            return null;
        }
        FSFile[] array = new CFile[files.length];
        for (int i = 0; i < files.length; i++) {
            array[i] = new CFile(files[i]);
        }
        return array;
    }

    @Override
    public FSFile[] listFiles(FilenameFilter filter) {
        File[] files = getPlainFile().listFiles(filter);
        if (files == null) {
            return null;
        }
        FSFile[] array = new CFile[files.length];
        for (int i = 0; i < files.length; i++) {
            array[i] = new CFile(files[i]);
        }
        return array;
    }

    @Override
    public FSFile[] listFiles(FileFilter filter) {
        File[] files = getPlainFile().listFiles(filter);
        if (files == null) {
            return null;
        }
        FSFile[] array = new CFile[files.length];
        for (int i = 0; i < files.length; i++) {
            array[i] = new CFile(files[i]);
        }
        return array;
    }

    @Override
    public boolean mkdir() {
        return getPlainFile().mkdir();
    }

    @Override
    public boolean mkdirs() {
        return getPlainFile().mkdirs();
    }

    @Override
    public boolean renameTo(FSFile dest) {
        boolean renamed = 
                getPlainFile().renameTo(((CFile) dest).getPlainFile());
        
        if (!renamed) {
            Path source = Paths.get(this.getAbsolutePath());
            Path destination = Paths.get(dest.getAbsolutePath());
            
            try {
                Files.move(source, destination);
                return true;
            } catch (IOException ex) {
                return false;
            } finally {
            }
        }
        
        return true;
    }

    @Override
    public boolean setLastModified(long time) {
        return getPlainFile().setLastModified(time);
    }

    @Override
    public boolean setReadOnly() {
        return getPlainFile().setReadOnly();
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        return getPlainFile().setWritable(writable, ownerOnly);
    }

    @Override
    public boolean setWritable(boolean writable) {
        return getPlainFile().setWritable(writable);
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        return getPlainFile().setReadable(readable, ownerOnly);
    }

    @Override
    public boolean setReadable(boolean readable) {
        return getPlainFile().setReadable(readable);
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        return getPlainFile().setExecutable(executable, ownerOnly);
    }

    @Override
    public boolean setExecutable(boolean executable) {
        return getPlainFile().setExecutable(executable);
    }

    @Override
    public boolean canExecute() {
        return getPlainFile().canExecute();
    }

    public static File[] listRoots() {
        return File.listRoots();
    }

    @Override
    public long getTotalSpace() {
        return getPlainFile().getTotalSpace();
    }

    @Override
    public long getFreeSpace() {
        return getPlainFile().getFreeSpace();
    }

    @Override
    public long getUsableSpace() {
        return getPlainFile().getUsableSpace();
    }

    @Override
    public int compareTo(FSFile dest) {
        return getPlainFile().compareTo(((CFile) dest).getPlainFile());
    }

    public static CFile createTempFile(
        String prefix,
        String suffix,
        File directory)
        throws IOException {
        return new CFile(File.createTempFile(prefix, suffix, directory));
    }

    public static CFile createTempFile(String prefix, String suffix)
        throws IOException {
        return new CFile(File.createTempFile(prefix, suffix));
    }

    @Override
    public FSFile getChild(FSFile location) {
        return this.getChild(location.getPath());
    }

    @Override
    public FSFile getChild(String path) {
        return new CFile(this, path);
    }

    @Override
    public LineReader getLineReader(Map<String, List<String>> cache)
        throws FileNotFoundException {
        LineReader lr = new LineReader(getPlainFile(), cache);
        return lr;
    }

    @Override
    public BufferedWriter getBufferedWriter() throws IOException {
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(getPlainFile()));
        return writer;
    }

    @Override
    public BufferedWriter getBufferedWriter(boolean b) throws IOException {
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(getPlainFile(), b));
        return writer;
    }

    @Override
    public BufferedReader getBufferedReader()
        throws FileNotFoundException {
        BufferedReader writer = new BufferedReader(
            new FileReader(getPlainFile()));
        return writer;
    }

    @Override
    public List<String> getLines() throws IOException {
        Path path = Paths.get(this.getAbsolutePath());
        return Files.readAllLines(path);
    }

    @Override
    public List<String> saveLines(List<String> lines) throws IOException {
        Path path = Paths.get(this.getAbsolutePath());
        Files.write(path, lines);
        return lines;
    }

    @Override
    public String saveString(String string) throws IOException {
        Path path = Paths.get(this.getAbsolutePath());
        LinkedList<String> it = new LinkedList<>();
        it.add(string);
        Files.write(path, it);
        return string;
    }

    /**
     * @return the plainFile
     */
    public File getPlainFile() {
        return plainFile;
    }
    
    public String toString() {
      return this.plainFile.getAbsolutePath();
    }
}
