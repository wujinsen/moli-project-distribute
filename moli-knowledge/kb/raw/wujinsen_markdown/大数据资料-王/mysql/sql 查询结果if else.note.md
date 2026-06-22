SELECT s.time AS TIME, 'ts投诉' AS sugestWal,

- r.region_name AS courseName, c.center_name AS centerName, cr.clas_name AS clasName, tsc.course_name AS courseName, stu.true_name AS studentName, stu.phone AS studentPhone, CASE s.flag

- WHEN 0 THEN '投诉'
- WHEN 1 THEN '建议' ELSE '表扬' END AS flag , st.type_name AS type_name,


- s.obj_name AS obj_name, dep.dept_name AS deptName, tel.name AS titleName, s.content AS content FROM sugest s LEFT JOIN center c ON c.center_id = s.center_id LEFT JOIN city ON city.city_id=c.city_id LEFT JOIN region r ON city.region_id=r.region_id LEFT JOIN sugest_type st ON st.type_id = s.sugest_type LEFT JOIN student stu ON s.student_id=stu.student_id LEFT JOIN clasrom cr ON stu.clas_id=cr.clas_id LEFT JOIN series_clas sc ON sc.series_clas_id = cr.series_clas_id LEFT JOIN tsc_course tsc ON sc.course_id = tsc.course_id LEFT JOIN employe e ON e.employe_id=s.obj_id LEFT JOIN dept dep ON e.dept_id=dep.dept_id LEFT JOIN title tel ON tel.title_id=e.title_id WHERE 1=1 AND s.time >= '2013-09-01 0  0  0' AND s.time < '2013-10-01 0  0  0'


