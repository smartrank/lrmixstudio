/**
 * Copyright (C) 2013, 2014 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.lrmixstudio.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to resolve filenames. If a filename is supplied that cannot be found,
 * the user is prompted to select for a file on their local system. This to
 * support reading of logfiles containing absolute paths from other users'
 * systems.
 *
 * @author dejong
 */
public class PathResolver {

    private static final Logger LOG = LoggerFactory.getLogger(PathResolver.class);
    private final Component _parent;
    private final HashMap<String, File> _pathRedirects = new HashMap<>();
    private File _currentPath;

    public PathResolver(Component parent) {
        _parent = parent;
        readPathRedirects();
    }

    /**
     * If the supplied filename does not exist, the user is asked to supply the
     * file's location on the current system. The modified filename is then
     * returned. If a file with the supplied name could be found, the filename
     * is returned unchanged.
     *
     * @param fileName The name of the file to find
     * @param alwaysAsk true if the resolver should always ask the user to
     * select a file, even if the file referenced by the fileName parameter
     * exists or a mapping for the filename was found.
     * @return The name of the file as indicated by the user if the supplied
     * file did not exist, or the original filename if the file was found.
     */
    String resolve(String fileName, boolean alwaysAsk) throws PathResolverAbortedException {
        File file = new File(fileName);
        if (!alwaysAsk && file.exists()) {
            return fileName;
        }
        String path = file.getParent();
        String name = file.getName();

        if (!alwaysAsk) {
            File redirect = _pathRedirects.get(path);
            if (redirect != null) {
                file = new File(redirect, name);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }

        JFileChooser chooser = new JFileChooser(ApplicationSettings.getCaseFilesPath());
        if (_pathRedirects.get(path) != null) {
            chooser.setCurrentDirectory(_pathRedirects.get(path));
        } else {
            chooser.setCurrentDirectory(_currentPath);
        }
        if (fileName.length() > 60) {
            chooser.setDialogTitle("Please locate '..." + fileName.substring(fileName.length() - 57) + "' on your machine");
        } else {
            chooser.setDialogTitle("Please locate '" + fileName + "' on your machine");
        }
        chooser.setSelectedFile(new File(fileName));
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(_parent)) {
            throw new PathResolverAbortedException();
        }
        _pathRedirects.put(path, chooser.getSelectedFile().getParentFile());
        _currentPath = chooser.getSelectedFile().getParentFile().getParentFile();
        storePathRedirects();
        return chooser.getSelectedFile().getAbsolutePath();
    }

    private void readPathRedirects() {
        try {
            Properties redirects = new Properties();
            redirects.load(new FileReader("redirects.properties"));
            Enumeration propertyNames = redirects.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String originalFileName = propertyNames.nextElement().toString();
                if ("currentDirectory".equalsIgnoreCase(originalFileName)) {
                    _currentPath = new File(redirects.getProperty(originalFileName));
                } else {
                    _pathRedirects.put(originalFileName, new File(redirects.getProperty(originalFileName)));
                }
            }
        } catch (IOException ex) {
            LOG.debug("Failed to load the redirection information.", ex);
        }
    }

    private void storePathRedirects() {
        try {
            Properties redirects = new Properties();
            for (String key : _pathRedirects.keySet()) {
                redirects.setProperty(key, _pathRedirects.get(key).getAbsolutePath());
            }
            redirects.setProperty("currentDirectory", _currentPath.getPath());
            redirects.store(new FileWriter("redirects.properties"), "This file contains mappings to resolve the file names read from log files created on other machines.");
        } catch (IOException ex) {
            LOG.warn("Failed to store the redirection information.", ex);
        }
    }

    /**
     * An exception class thrown only from the resolve method of the
     * PathResolver indicating that the user opted to abort the process.
     */
    public static class PathResolverAbortedException extends Exception {

        /**
         * We want other classes to explicitly catch this exception, but they
         * have no business creating a new instance of it.
         */
        private PathResolverAbortedException() {
        }
    }
}
