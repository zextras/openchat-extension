package com.zextras.lib;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class DateUtils
{
  public static long parseBirthday( String strdate ) throws ParseException
  {
    DateTimeFormatter df;
    if (strdate.startsWith("--"))
    {
      df = DateTimeFormat.forPattern( "--MM-dd" );
      return df.parseDateTime(strdate).withYear(1604).getMillis();
    }
    else
    {
      return DateTimeFormat.forPattern( "yyyy-MM-dd" ).parseDateTime(strdate).getMillis();
    }
  }

  public static String formatBirthdayExcludeYear( long date )
  {
    DateTimeFormatter df = DateTimeFormat.forPattern("--MM-dd");
    return formatBirthday( date, df);
  }

  public static String formatBirthday( long date )
  {
    DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-MM-dd");
    return formatBirthday( date, df);
  }

  public static String formatBirthday( long date, DateTimeFormatter df )
  {
    DateTime dateTime = new DateTime(date, DateTimeZone.UTC);
    return df.print( dateTime );
  }

  public static long parseUTCDate( String strdate ) throws ParseException
  {
    return parseUTCDate( strdate, TimeZone.getTimeZone("UTC") );
  }

  public static long parseUTCDate( String strdate, TimeZone tz ) throws ParseException
  {
    String format = "";

    if(strdate.contains("-"))
    {
      format += "yyyy-MM-dd";
    }
    else
    {
      format += "yyyyMMdd";
    }

    format += "'T'";

    if(strdate.contains(":"))
    {
      format += "HH:mm:ss";
    }
    else
    {
      format += "HHmmss";
    }

    if(strdate.contains("."))
    {
      format += ".SSS";
    }

    format += "'Z'";

    SimpleDateFormat df = new SimpleDateFormat( format );
    df.setTimeZone( tz );

    return df.parse( strdate ).getTime();
  }
}
