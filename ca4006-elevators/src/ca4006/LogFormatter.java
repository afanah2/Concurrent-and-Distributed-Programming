package ca4006;

import java.util.logging.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LogFormatter extends Formatter
{
    private final DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");

    public String format(LogRecord r)
    {
        String pkgClass = r.getSourceClassName() + "." + r.getSourceMethodName();
        String date = df.format(new Date(r.getMillis()));
        String threadName = Thread.currentThread().getName();
        String msg = formatMessage(r);
        String lvl = r.getLevel().toString();
        return String.format("%-14s%-9s%-40s%-12s%s\n", date, lvl, pkgClass, threadName, msg);
    }
}
