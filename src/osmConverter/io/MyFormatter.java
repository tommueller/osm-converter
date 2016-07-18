package osmConverter.io;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class MyFormatter extends Formatter {
    public String format(LogRecord log) {
	String date = calcDate(log.getMillis());

	String level = log.getLevel().getName();
	String logmessage = "[" + level + "]" + "\t[" + date + "]\t";
	logmessage = logmessage + log.getMessage() + "\r\n";

	Throwable thrown = log.getThrown();
	if (thrown != null) {
	    logmessage = logmessage + thrown.toString();
	}
	return logmessage;
    }

    /**
     * @param dateMilliSecs
     * @return
     */
    private String calcDate(long dateMilliSecs) {
	SimpleDateFormat date_format = new SimpleDateFormat(
		"dd.MM.yyyy HH:mm:ss:SS");
	Date resultdate = new Date(dateMilliSecs);

	return date_format.format(resultdate);
    }
}