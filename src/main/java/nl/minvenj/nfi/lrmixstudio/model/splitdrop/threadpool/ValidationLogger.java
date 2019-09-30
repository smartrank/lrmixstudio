package nl.minvenj.nfi.lrmixstudio.model.splitdrop.threadpool;
import org.slf4j.Logger;
import org.slf4j.Marker;

import nl.minvenj.nfi.lrmixstudio.gui.ApplicationSettings;

/**
 * A class that handles logging to the validation log.
 *
 * @author Netherlands Forensic Institute
 */
public class ValidationLogger implements Logger {
    private final Logger _logger;

    public ValidationLogger(Logger logger) {
        _logger = logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return _logger.isTraceEnabled();
    }
    @Override
    public void trace(String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(string);
        }
    }
    @Override
    public void trace(String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(string, o);
        }
    }
    @Override
    public void trace(String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(string, o, o1);
        }
    }
    @Override
    public void trace(String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
                _logger.trace(string, os);
            }
    }
    @Override
    public void trace(String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(string, thrwbl);
        }
    }
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return _logger.isTraceEnabled(marker);
    }
    @Override
    public void trace(Marker marker, String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(marker, string);
        }
    }
    @Override
    public void trace(Marker marker, String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(marker, string, o);
        }
    }
    @Override
    public void trace(Marker marker, String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(marker, string, o, o1);
        }
    }
    @Override
    public void trace(Marker marker, String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(marker, string, os);
        }
    }
    @Override
    public void trace(Marker marker, String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.trace(marker, string, thrwbl);
        }
    }
    @Override
    public boolean isDebugEnabled() {
        return _logger.isDebugEnabled();
    }
    @Override
    public void debug(String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(string);
        }
    }
    @Override
    public void debug(String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(string, o);
        }
    }
    @Override
    public void debug(String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(string, o, o1);
        }
    }
    @Override
    public void debug(String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(string, os);
        }
    }
    @Override
    public void debug(String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(string, thrwbl);
        }
    }
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return _logger.isDebugEnabled(marker);
    }
    @Override
    public void debug(Marker marker, String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(marker, string);
        }
    }
    @Override
    public void debug(Marker marker, String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(marker, string, o);
        }
    }
    @Override
    public void debug(Marker marker, String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(marker, string, o, o1);
        }
    }
    @Override
    public void debug(Marker marker, String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(marker, string, os);
        }
    }
    @Override
    public void debug(Marker marker, String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.debug(marker, string, thrwbl);
        }
    }
    @Override
    public boolean isInfoEnabled() {
        return _logger.isInfoEnabled();
    }
    @Override
    public void info(String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(string);
        }
    }
    @Override
    public void info(String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(string, o);
        }
    }
    @Override
    public void info(String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(string, o, o1);
        }
    }
    @Override
    public void info(String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(string, os);
        }
    }
    @Override
    public void info(String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(string, thrwbl);
        }
    }
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return _logger.isInfoEnabled(marker);
    }
    @Override
    public void info(Marker marker, String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(marker, string);
        }
    }
    @Override
    public void info(Marker marker, String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(marker, string, o);
        }
    }
    @Override
    public void info(Marker marker, String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(marker, string, o, o1);
        }
    }
    @Override
    public void info(Marker marker, String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(marker, string, os);
        }
    }
    @Override
    public void info(Marker marker, String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.info(marker, string, thrwbl);
        }
    }
    @Override
    public boolean isWarnEnabled() {
        return _logger.isWarnEnabled();
    }
    @Override
    public void warn(String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(string);
        }
    }
    @Override
    public void warn(String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(string, o);
        }
    }
    @Override
    public void warn(String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(string, os);
        }
    }
    @Override
    public void warn(String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(string, o, o1);
        }
    }
    @Override
    public void warn(String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(string, thrwbl);
        }
    }
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return _logger.isWarnEnabled(marker);
    }
    @Override
    public void warn(Marker marker, String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(marker, string);
        }
    }
    @Override
    public void warn(Marker marker, String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(marker, string, o);
        }
    }
    @Override
    public void warn(Marker marker, String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(marker, string, o, o1);
        }
    }
    @Override
    public void warn(Marker marker, String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(marker, string, os);
        }
    }
    @Override
    public void warn(Marker marker, String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.warn(marker, string, thrwbl);
        }
    }
    @Override
    public boolean isErrorEnabled() {
        return _logger.isErrorEnabled();
    }
    @Override
    public void error(String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(string);
        }
    }
    @Override
    public void error(String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(string, o);
        }
    }
    @Override
    public void error(String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(string, o, o1);
        }
    }
    @Override
    public void error(String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(string, os);
        }
    }
    @Override
    public void error(String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(string, thrwbl);
        }
    }
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return _logger.isErrorEnabled(marker);
    }
    @Override
    public void error(Marker marker, String string) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(marker, string);
        }
    }
    @Override
    public void error(Marker marker, String string, Object o) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(marker, string, o);
        }
    }
    @Override
    public void error(Marker marker, String string, Object o, Object o1) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(marker, string, o, o1);
        }
    }
    @Override
    public void error(Marker marker, String string, Object... os) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(marker, string, os);
        }
    }
    @Override
    public void error(Marker marker, String string, Throwable thrwbl) {
        if (ApplicationSettings.isValidationMode()) {
            _logger.error(marker, string, thrwbl);
        }
    }
    @Override
    public String getName() {
        return _logger.getName();
    }
}
