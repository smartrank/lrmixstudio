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

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.domain.PopulationStatistics;

public class ApplicationSettings {
    private ApplicationSettings() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationSettings.class);
    private static final Properties SETTINGS = new Properties();
    private static final Properties STATIC_SETTINGS = new Properties();
    /**
     * Dynamic properties sourced from the LRmixStudio.properties file in the
     * install directory or the user's home directory
     */
    private static final String SETTINGS_FILENAME = "LRmixStudio.properties";
    private static final String DEFAULT_SETTINGS_FILENAME = new File(SETTINGS_FILENAME).getAbsolutePath();
    private static final String USER_HOME_SETTINGS_FILENAME = new File(new File(System.getProperty("user.home"), ".lrmixstudio"), SETTINGS_FILENAME).getAbsolutePath();
    
    private static final String CASEFILES_PATH = "caseFilesPath";
    private static final String ALLELEFREQUENCIES_PATH = "alleleFrequenciesPath";
    private static final String RARE_ALLELE_FREQUENCY = "rareAlleleFrequency";
    private static final String THREADCOUNT = "threadCount";
    private static final String VALIDATION_MODE = "validationMode";
    private static final String HIGHLIGHT_COLOR = "highlightColor";
    private static final String HIGHLIGHT_BACKGROUND_COLOR = "highlightBackgroundColor";
    private static final String HIGHLIGHT_UNDERLINE = "highlightUnderline";
    private static final String HIGHLIGHT_BOLD = "highlightBold";
    private static final String HIGHLIGHT_ITALIC = "highlightItalic";
    private static final int DEFAULT_THREADCOUNT = Runtime.getRuntime().availableProcessors();
    private static final String LOSTTIMETHRESHOLD = "acceptableLostTimeOnSessionClear";
    private static final String MRU = "mostRecentlyUsed";
    private static final String MRU_MAX_ENTRIES = "maxMostRecentlyUsedEntries";
    private static final long DEFAULT_LOSTTIMETHRESHOLD = 60000;
    private static final int DEFAULT_MAX_MRU_ENTRIES = 4;
    private static final String ADVANCED_MODE = "advancedMode";
    private static final String LOGALLNONCONTRIBUTORLRS = "logAllNonContributorLRs";
    private static final String LATEST_DROPIN = "latestDropin";
    private static final Double DEFAULT_DROPIN = 0.05;
    private static final String LATEST_THETA = "latestTheta";
    private static final Double DEFAULT_THETA = 0.01;
    private static final String FONT_SIZE = "fontSize";
    private static final String DEFAULT_FONT_SIZE = "11";

    private static final String REPORT_TEMPLATE_FILENAME = "reportTemplateFilename";
    private static final String DEFAULT_REPORT_TEMPLATE_FILENAME = "report/LRmixStudio.jrxml";

    private static String _settingsFileName = System.getProperty("lrmixStudioSettings");
    private static long _lastModified;

    /**
     * Static properties sourced from LRmixStudio.properties in the resources
     * package
     */
    private static final String VERSION = "version";
    private static final String ICON = "icon.";
    private static final String TRAYICON = "trayIcon.";
    private static final String TRAYICON_IDLE = TRAYICON + "idle.";
    private static final String TRAYICON_BUSY = TRAYICON + "busy.";
    private static final String SHOWGRID = "showGrid";
    private static final String SAMPLES_SHOWGRID = "samples." + SHOWGRID;
    private static final String OVERVIEW_SHOWGRID = "overview." + SHOWGRID;
    private static final String MAXUNKNOWNS = "maxUnknowns";
    private static final String DEFAULT_MAXUNKNOWNS = "4";
    private static final AtomicBoolean IS_STORE_ERROR_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean IS_LOAD_SOURCE_LOGGED = new AtomicBoolean();

    private static void load() {
        if (_settingsFileName == null) {
            if(new File(DEFAULT_SETTINGS_FILENAME).canWrite()) {
                load(DEFAULT_SETTINGS_FILENAME);
            } else {
                if(!load(USER_HOME_SETTINGS_FILENAME))
                    load(DEFAULT_SETTINGS_FILENAME);
            }
        }
        else {
            load(_settingsFileName);
        }
    }

    private static boolean load(final String fileName) {
        File file = new File(fileName);
        if (fileName.equals(_settingsFileName) && _lastModified == file.lastModified()) {
            return true;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            SETTINGS.load(fis);
            if(!IS_LOAD_SOURCE_LOGGED.getAndSet(true)) {
                LOG.info("Loaded settings from {}", fileName);
            }
            return true;
        }
        catch (final FileNotFoundException ex) {
            LOG.debug("Properties file {} does not exist yet.", fileName);
        }
        catch (final Exception ex) {
            LOG.debug("Error loading properties file: \n" + ex.getLocalizedMessage());
        }
        return false;
    }

    private static void store() {
        if(_settingsFileName==null) {
            IS_LOAD_SOURCE_LOGGED.set(false);
            if (!store(DEFAULT_SETTINGS_FILENAME)) {
                if(!store(USER_HOME_SETTINGS_FILENAME)) {
                    if(!IS_STORE_ERROR_LOGGED.getAndSet(true)) {
                        LOG.error("Could not store settings in either '{}' or '{}'", DEFAULT_SETTINGS_FILENAME, USER_HOME_SETTINGS_FILENAME);
                    }
                }
            }
        } else {
            if(!store(_settingsFileName)) {
                if(!IS_STORE_ERROR_LOGGED.getAndSet(true)) {
                    LOG.error("Could not store settings in '{}'", _settingsFileName);
                }
            }
        }
    }

    private static boolean store(final String fileName) {
        File propertiesFileDirectory = new File(fileName).getParentFile();
        if(propertiesFileDirectory!=null) {
            propertiesFileDirectory.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            SETTINGS.store(fos, "Created by LRmixStudio v" + LRmixStudio.class.getPackage().getImplementationVersion());
            _settingsFileName = fileName;
            _lastModified = new File(fileName).lastModified();
            return true;
        }
        catch (final Exception ex) {
            LOG.debug("Error writing properties to " + fileName + " - " + ex.getClass().getSimpleName() + " - " + ex.getLocalizedMessage());
        }
        return false;
    }

    private static void removeByPrefix(final String prefix) {
        final ArrayList<String> toRemove = new ArrayList<>();
        for (final Object key : SETTINGS.keySet()) {
            if (key.toString().startsWith(prefix)) {
                toRemove.add(key.toString());
            }
        }

        for (final String removeThisKey : toRemove) {
            SETTINGS.remove(removeThisKey);
        }

        store();
    }

    private static void set(final String key, final String value) {
        if (value != null) {
            SETTINGS.put(key, value);
        }
        else {
            SETTINGS.remove(key);
        }
        store();
    }

    private static String get(final String key, final String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            load();
            value = SETTINGS.getProperty(key);
            if (value == null || value.isEmpty()) {
                set(key, defaultValue);
                value = defaultValue;
            }
        }
        return value;
    }

    public static void setHighlightColor(final Color c) {
        if (c == null) {
            set(HIGHLIGHT_COLOR, "");
        }
        else {
            set(HIGHLIGHT_COLOR, Integer.toHexString(c.getRGB()));
        }
    }

    public static Color getHighlightColor() {
        String colorString = get(HIGHLIGHT_COLOR, "FFFFFF");
        if (colorString.length() > 6) {
            colorString = colorString.substring(colorString.length() - 6);
        }
        return new Color(Integer.parseInt(colorString, 16));
    }

    public static boolean isSetHighlightColor() {
        return !get(HIGHLIGHT_COLOR, "").isEmpty();
    }

    public static void setHighlightBackgroundColor(final Color c) {
        if (c == null) {
            set(HIGHLIGHT_BACKGROUND_COLOR, "");
        }
        else {
            set(HIGHLIGHT_BACKGROUND_COLOR, Integer.toHexString(c.getRGB()));
        }
    }

    public static Color getHighlightBackgroundColor() {
        String colorString = get(HIGHLIGHT_BACKGROUND_COLOR, "FF0000");
        if (colorString.length() > 6) {
            colorString = colorString.substring(colorString.length() - 6);
        }
        return new Color(Integer.parseInt(colorString, 16));
    }

    public static boolean isSetHighlightBackgroundColor() {
        return !get(HIGHLIGHT_BACKGROUND_COLOR, "").isEmpty();
    }

    public static void setHighlightUnderlined(final boolean isUnderlined) {
        set(HIGHLIGHT_UNDERLINE, "" + isUnderlined);
    }

    public static boolean getHighlightUnderlined() {
        final String isUnderlined = get(HIGHLIGHT_UNDERLINE, "false");
        return Boolean.parseBoolean(isUnderlined);
    }

    public static void setHighlightBold(final boolean isBold) {
        set(HIGHLIGHT_BOLD, "" + isBold);
    }

    public static boolean getHighlightBold() {
        final String isBold = get(HIGHLIGHT_BOLD, "true");
        return Boolean.parseBoolean(isBold);
    }

    public static void setHighlightItalic(final boolean isItalic) {
        set(HIGHLIGHT_ITALIC, "" + isItalic);
    }

    public static boolean getHighlightItalic() {
        final String isItalic = get(HIGHLIGHT_ITALIC, "false");
        return Boolean.parseBoolean(isItalic);
    }

    private static boolean _validationMode = false;

    public static void setValidationMode(final boolean mode) {
        _validationMode = mode;
    }

    public static boolean isValidationMode() {
        return _validationMode;
    }

    public static String getCaseFilesPath() {
        return get(CASEFILES_PATH, "");
    }

    public static void setCaseFilesPath(final String path) {
        set(CASEFILES_PATH, path);
    }

    public static String getAlleleFrequenciesPath() {
        return get(ALLELEFREQUENCIES_PATH, "");
    }

    public static void setAlleleFrequenciesPath(final String path) {
        set(ALLELEFREQUENCIES_PATH, path);
    }

    public static void setLogAllNonConLRs(final boolean logNonConLRs) {
        set(LOGALLNONCONTRIBUTORLRS, Boolean.toString(logNonConLRs));
    }

    public static boolean isLogAllNonConLRs() {
        final String isLogged = get(LOGALLNONCONTRIBUTORLRS, "false");
        return Boolean.parseBoolean(isLogged);
    }

    public static int getMaxUnknowns() {
        try {
            return Integer.parseInt(get(MAXUNKNOWNS, DEFAULT_MAXUNKNOWNS));
        }
        catch (final Exception e) {
            set(MAXUNKNOWNS, DEFAULT_MAXUNKNOWNS);
            return Integer.parseInt(DEFAULT_MAXUNKNOWNS);
        }
    }

    public static String getRareAlleleFrequency(final PopulationStatistics statistics) {
        return get(RARE_ALLELE_FREQUENCY + (statistics == null ? "" : statistics.getFileHash()), "" + PopulationStatistics.DEFAULT_FREQUENCY);
    }

    public static void setRareAlleleFrequency(final PopulationStatistics statistics, final double freq) {
        set(RARE_ALLELE_FREQUENCY + (statistics == null ? "" : statistics.getFileHash()), "" + freq);
    }

    public static void setFontSize(final int fontSize) {
        set(FONT_SIZE, "" + fontSize);
    }

    public static int getFontSize() {
        return Integer.parseInt(get(FONT_SIZE, DEFAULT_FONT_SIZE));
    }

    public static void setThreadCount(final int threadCount) {
        set(THREADCOUNT, "" + threadCount);
    }

    public static Iterable<String> getMostRecentlyUsed() {
        final ArrayList<String> mru = new ArrayList<>();
        int idx = 0;
        String mruSession;
        while (idx < getMaxMostRecentlyUsedEntries() && !(mruSession = get(MRU + idx, "")).isEmpty()) {
            idx++;
            mru.add(mruSession);
        }
        return mru;
    }

    public static void addMostRecentlyUsed(final String fileName) {
        final ArrayList<String> mru = new ArrayList<>();
        mru.add(fileName);
        int idx = 0;
        String mruSession;
        while (idx < getMaxMostRecentlyUsedEntries() && !(mruSession = get(MRU + idx, "")).isEmpty()) {
            idx++;
            if (!mruSession.equalsIgnoreCase(fileName)) {
                mru.add(mruSession);
            }
        }
        idx = 0;
        for (final String session : mru) {
            set(MRU + idx++, session);
        }
    }

    public static void removeMostRecentlyUsed(final String fileName) {
        final ArrayList<String> mru = new ArrayList<>();
        int idx = 0;
        String mruSession;
        while (idx < getMaxMostRecentlyUsedEntries() && !(mruSession = get(MRU + idx, "")).isEmpty()) {
            idx++;
            if (!mruSession.equalsIgnoreCase(fileName)) {
                mru.add(mruSession);
            }
        }
        removeByPrefix(MRU);
        idx = 0;
        for (final String session : mru) {
            set(MRU + idx++, session);
        }
    }

    public static int getThreadCount() {
        final String threadCount = get(THREADCOUNT, "");
        try {
            return Integer.parseInt(threadCount);
        }
        catch (final NumberFormatException nfe) {
            return DEFAULT_THREADCOUNT;
        }
    }

    public static void setLostTimeThreshold(final long threshold) {
        set(LOSTTIMETHRESHOLD, "" + threshold);
    }

    public static long getLostTimeThreshold() {
        final String threshold = get(LOSTTIMETHRESHOLD, "");
        try {
            return Long.parseLong(threshold);
        }
        catch (final NumberFormatException nfe) {
            return DEFAULT_LOSTTIMETHRESHOLD;
        }
    }

    public static void setMaxMostRecentlyUsedEntries(final int maxMru) {
        set(MRU_MAX_ENTRIES, "" + maxMru);
    }

    public static int getMaxMostRecentlyUsedEntries() {
        final String maxMru = get(MRU_MAX_ENTRIES, "" + DEFAULT_MAX_MRU_ENTRIES);
        try {
            return Integer.parseInt(maxMru);
        }
        catch (final NumberFormatException nfe) {
            return DEFAULT_MAX_MRU_ENTRIES;
        }
    }

    private static void staticInit() {
        if (STATIC_SETTINGS.isEmpty()) {
            try {
                STATIC_SETTINGS.load(ApplicationSettings.class.getResourceAsStream("/resources/LRmixStudio.properties"));
            }
            catch (final IOException ex) {
            }
        }
    }

    public static String getProgramVersion() {
        final String version = LRmixStudio.class.getPackage().getImplementationVersion();
        if (version == null)
            return "Debug";
        return version;
    }

    public static String getIcon(final int idx) {
        staticInit();
        return STATIC_SETTINGS.getProperty(ICON + idx);
    }

    public static String getIdleTrayIcon(final int idx) {
        staticInit();
        return STATIC_SETTINGS.getProperty(TRAYICON_IDLE + idx);
    }

    public static String getBusyTrayIcon(final int idx) {
        staticInit();
        return STATIC_SETTINGS.getProperty(TRAYICON_BUSY + idx);
    }

    public static boolean isOverviewGridShown() {
        staticInit();
        return Boolean.parseBoolean(STATIC_SETTINGS.getProperty(OVERVIEW_SHOWGRID, "true"));
    }

    public static Color getColor(final String propertyName) {
        staticInit();
        final String colorString = STATIC_SETTINGS.getProperty(propertyName, "255,255,255");
        final String[] components = colorString.split(",");
        return new Color(Integer.parseInt(components[0]), Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }

    public static Color getOverviewEvenRowColor() {
        return getColor("overview.color.evenRows");
    }

    public static Color getOverviewOddRowColor() {
        return getColor("overview.color.oddRows");
    }

    public static Color getOverviewReplicateRowColor() {
        return getColor("overview.color.replicateRows");
    }

    public static Color getOverviewReplicateRowTextColor() {
        return getColor("overview.color.replicateRows.text");
    }

    public static Color getEvenRowColor(final String name) {
        return getColor(name + ".color.evenRows");
    }

    public static Color getOddRowColor(final String name) {
        return getColor(name + ".color.oddRows");
    }

    public static boolean isAdvancedMode() {
        return "true".equalsIgnoreCase(get(ADVANCED_MODE, "false"));
    }

    public static Double getLatestDropIn() {

        final String dropIn = get(LATEST_DROPIN, DEFAULT_DROPIN.toString());
        try {
            return Double.valueOf(dropIn);
}
        catch (final NumberFormatException nfe) {
            return DEFAULT_DROPIN;
        }
    }

    public static void setLatestDropIn(final Double dropIn) {
        set(LATEST_DROPIN, dropIn.toString());
    }

    public static Double getLatestTheta() {

        final String theta = get(LATEST_THETA, DEFAULT_THETA.toString());
        try {
            return Double.valueOf(theta);
        }
        catch (final NumberFormatException nfe) {
            return DEFAULT_THETA;
        }
    }

    public static void setLatestTheta(final Double theta) {
        set(LATEST_THETA, theta.toString());
    }

    public static String getReportTemplateFilename() {
        return get(REPORT_TEMPLATE_FILENAME, DEFAULT_REPORT_TEMPLATE_FILENAME);
}
}
