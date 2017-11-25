package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * utility class to manipulate dates
 */
public class DateUtils {

    private static String ES_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    /**
     * convert date to elasticsearch string format
     * @param d date to convert
     * @return string date to index
     */
    public static String convertDateToEsFormat(Date d){
        return new SimpleDateFormat(ES_DATE_FORMAT).format(d);
    }

}
