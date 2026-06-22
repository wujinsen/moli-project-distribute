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


import java.security.MessageDigest;

/**

- * MD5加密类

*

- */


public class MD5Encoding {

/** * * */

private MD5Encoding()

{ }

/**

- * 加密算法MD5

*

- * @param text

- * 明⽂

- * @return String 密⽂

- */


public ﬁnal static String encoding(String text) {

char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',

'e', 'f' }; String encodingStr = null; try {

byte[] strTemp = text.getBytes(); MessageDigest mdTemp = MessageDigest.getInstance("MD5"); mdTemp.update(strTemp); byte[] md = mdTemp.digest();

- int j = md.length; char str[] = new char[j * 2];

- int k = 0; for (int i = 0; i < j; i++) {


byte byte0 = md[i]; str[k++] = hexDigits[byte0 >>> 4 & 0xf]; str[k++] = hexDigits[byte0 & 0xf];

} encodingStr = new String(str);

} catch (Exception e)

{ } return encodingStr;

}

public static void main(String[] areg)

- 54.
- 55.
- 56.
- 57.
- 58.


{

MD5Encoding md5 = new MD5Encoding(); md5.encoding("admin");

} }

