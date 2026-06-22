⽇期转换成时间戳 select ( to_date('2018-03-02 0  0  0',' y- m-d h24:mi:s')-8/24 - to_date('1970-01-01

0  0  0',' y- m-d h24:mi:s') )* 24*60*60*1 0 from dual

时间戳转换成时间

SELECT TO_CHAR( 12070645 0 / (1 0 * 60 * 60 * 24) + TO_DATE('1970-01-01 08  0  0', ' Y- M-D H MI  S'), ' Y- M-D H MI  S') AS CDATE FROM dual;

