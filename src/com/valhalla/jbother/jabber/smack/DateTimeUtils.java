package com.valhalla.jbother.jabber.smack;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * User: luke
 * Date: 2004-12-30
 * Time: 23:24:44
 */

/**
 * simple util class that should be able to handle date and time for jabber
 * based on JEP-0082 (Jabber Date adn Time Profiles)
 */

public class DateTimeUtils {

  private static final String fId = "$Id$";

  // format used in JEP. The final 'Z' is for Zulu time
  public static final String DATETIMEFORMAT = "yyyy-MM-dd'T'hh:mm:ss'Z'";

  public static String getDateTime()
  {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATETIMEFORMAT);
    TimeZone timeZone = cal.getTimeZone();

    return dateTimeFormat.format(new Date(cal.getTimeInMillis()
        - timeZone.getOffset(cal.getTimeInMillis())));
  }
}
