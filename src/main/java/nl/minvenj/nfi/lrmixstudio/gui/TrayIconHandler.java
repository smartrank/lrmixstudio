/**
 * Copyright (C) 2013-2015 Netherlands Forensic Institute
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

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataChangeListener;
import nl.minvenj.nfi.lrmixstudio.model.ConfigurationDataElement;

public class TrayIconHandler implements ConfigurationDataChangeListener, ApplicationStateChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(TrayIconHandler.class);
    private final TrayIcon trayIcon;
    private final List<Image> busyIcons = new ArrayList<>();
    private final List<Image> idleIcons = new ArrayList<>();
    private String caption;
    private final SessionData session;
    private final LRmixStudio parent;
    private int oldProgressIconIndex;

    public TrayIconHandler(final LRmixStudio parent, final SessionData session) {
        this.session = session;
        this.parent = parent;
        caption = "LRmixStudio v" + ApplicationSettings.getProgramVersion();
        trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(ApplicationSettings.getIdleTrayIcon(0))), caption);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent.getState() == Frame.ICONIFIED) {
                    parent.setState(Frame.NORMAL);
                } else {
                    parent.toFront();
                    parent.repaint();
                }
            }
        });

        if (SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException ex) {
                LOG.warn("Error adding tray icon to system tray", ex);
            }
        }

        int idx = 0;
        String name = ApplicationSettings.getIdleTrayIcon(idx++);
        while (name != null) {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource(name));
            idleIcons.add(icon);
            name = ApplicationSettings.getIdleTrayIcon(idx++);
        }

        idx = 0;
        name = ApplicationSettings.getBusyTrayIcon(idx++);
        while (name != null) {
            Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource(name));
            busyIcons.add(icon);
            name = ApplicationSettings.getBusyTrayIcon(idx++);
        }

        session.addDataChangeListener(this);
        session.addStateChangeListener(this);
    }

    @Override
    public void dataChanged(ConfigurationDataElement element) {
        switch (element) {
            case CASENUMBER:
                caption = "LRmixStudio v" + ApplicationSettings.getProgramVersion() + " Case " + session.getCaseNumber();
                trayIcon.setToolTip(caption + "\n" + session.getApplicationState().getDescription());
                break;
            case PERCENTREADY:
                int newProgressIconIndex = ((session.getProgress() * busyIcons.size()) / 100) % busyIcons.size();
                if (newProgressIconIndex != oldProgressIconIndex) {
                    oldProgressIconIndex = newProgressIconIndex;
                    trayIcon.setImage(busyIcons.get(newProgressIconIndex));
                }
                trayIcon.setToolTip(caption + "\n" + session.getApplicationState().getDescription() + "\n" + session.getProgress() + "% done");
                break;
            case STATUS_MESSAGE:
                if (!parent.isActive() && !"".equalsIgnoreCase(session.getStatusMessage())) {
                    trayIcon.displayMessage(caption, removeHtml(session.getStatusMessage()), TrayIcon.MessageType.INFO);
                }
                break;
            case ERROR_MESSAGE:
                if (!parent.isActive() && !"".equalsIgnoreCase(session.getErrorMessage())) {
                    trayIcon.displayMessage(caption, removeHtml(session.getErrorMessage()), TrayIcon.MessageType.ERROR);
                }
                break;
        }
    }

    @Override
    public void applicationStateChanged(APP_STATE newState) {
        if (trayIcon != null) {
            trayIcon.setToolTip(caption + "\n" + newState.getDescription());
            if (!newState.isActive()) {
                trayIcon.setImage(idleIcons.get(0));
                dataChanged(ConfigurationDataElement.CASENUMBER);
            }
        }
    }

    /**
     * Removes any HTML/XML tags from the supplied string.
     */
    private String removeHtml(String string) {
        return string.replaceAll("\\<\\s*[bB][rR]\\s*/?\\s*\\>", "\n").replaceAll("\\<[^\\>]+\\>", "");
    }
}
