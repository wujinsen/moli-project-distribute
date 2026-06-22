./configure时⽼是报！ checking opensl/sl.h usability. no checking opensl/sl.h presence. no checking for opensl/sl.h. no configure: eror:

! OpenSL is not properly instaled on your system. ! ! Can not include OpenSL headers files.

解决： yum instal -y opensl opensl-devel

