String—>Date⽅法⼀：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


String dateString = "2012-12-06 "; try {

SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd "); Date date = sdf.parse(dateString);

} catch (ParseException e) {

System.out.println(e.getMessage()); }

String—>Date⽅法⼆：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


import java.text.ParseException; import java.text.SimpleDateFormat; import java.util.Calendar; import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**

- * ⽇期Util类

*

- * @author calvin

- */


public class DateUtil {

private static String defaultDatePattern = "yyyy-MM-dd ";

/**

- * 获得默认的 date pattern

- */


public static String getDatePattern() {

return defaultDatePattern; }

/**

- * 返回预设Format的当前⽇期字符串

- */


public static String getToday() {

Date today = new Date(); return format(today);

}

/**

- * 使⽤预设Format格式化Date成字符串

- */


public static String format(Date date) {

- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.


return date == null ? " " : format(date, getDatePattern()); }

/**

- * 使⽤参数Format格式化Date成字符串

- */


public static String format(Date date, String pattern) {

return date == null ? " " : new SimpleDateFormat(pattern).format(date); }

/**

- * 使⽤预设格式将字符串转为Date

- */


public static Date parse(String strDate) throws ParseException {

return StringUtils.isBlank(strDate) ? null : parse(strDate,

getDatePattern()); }

/**

- * 使⽤参数Format将字符串转为Date

- */


public static Date parse(String strDate, String pattern)

throws ParseException {

return StringUtils.isBlank(strDate) ? null : new SimpleDateFormat(

pattern).parse(strDate); }

/**

- * 在⽇期上增加数个整⽉

- */


public static Date addMonth(Date date, int n) {

Calendar cal = Calendar.getInstance(); cal.setTime(date); cal.add(Calendar.MONTH, n); return cal.getTime();

}

public static String getLastDayOfMonth(String year, String month) {

Calendar cal = Calendar.getInstance(); // 年 cal.set(Calendar.YEAR, Integer.parseInt(year)); // ⽉，因为Calendar⾥的⽉是从0开始，所以要-1 // cal.set(Calendar.MONTH, Integer.parseInt(month) - 1); // ⽇，设为⼀号 cal.set(Calendar.DATE, 1); // ⽉份加⼀，得到下个⽉的⼀号 cal.add(Calendar.MONTH, 1); // 下⼀个⽉减⼀为本⽉最后⼀天

- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.


cal.add(Calendar.DATE, -1); return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));// 获得⽉末是⼏号

}

public static Date getDate(String year, String month, String day)

throws ParseException {

String result = year + "- "

+ (month.length() == 1 ? ("0 " + month) : month) + "- "

+ (day.length() == 1 ? ("0 " + day) : day); return parse(result);

} }

Date—>String

- 1.
- 2.
- 3.
- 4.


String sdate; Date ddate; …… sdate=(new SimpleDateFormat("yyyy-MM-dd")).format(ddate);

SimpleDateFormat函数语法：

- G 年代标志符

- y 年 M ⽉ d ⽇ h 时 在上午或下午 (1~12)

H 时 在⼀天中 (0~23) m 分 s 秒 S 毫秒

- E 星期 D ⼀年中的第⼏天

- F ⼀⽉中第⼏个星期⼏ w ⼀年中第⼏个星期 W ⼀⽉中第⼏个星期 a 上午 / 下午 标记符 k 时 在⼀天中 (1~24) K 时 在上午或下午 (0~11)


- z 时区




常⻅标准的写法"yyyy-MM-dd HH:mm:ss",注意⼤⼩写，时间是24⼩时制，24⼩时制转换成12⼩ 时制只需将HH改成hh,不需要另外的函数。

