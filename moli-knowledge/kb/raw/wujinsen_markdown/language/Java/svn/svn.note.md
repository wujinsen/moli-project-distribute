svn安装: 安装svn之前需要额外安装必要依赖包 apr安装:

./conﬁgure --preﬁx=/usr/local/svn/apr make make install apr-util安装:

./conﬁgure --preﬁx=/usr/local/svn/aprutil --with-apr=/usr/local/svn/apr make make install

sqlite安装:

./conﬁgure --preﬁx=/usr/local/svn/sqlite make make install zlib安装:

./conﬁgure --preﬁx=/usr/local/svn/zlib make make install

zlib⽬录: CFLAGS="-O3 -fPIC" ./conﬁgure --preﬁx=/usr/local/svn/zlib make make install

svn安装:

./conﬁgure --preﬁx=/usr/local/svn/svn1.7.5 --with-apr=/usr/local/svn/apr --with-aprutil=/usr/local/svn/aprutil --with-sqlite=/usr/local/svn/sqlite --with-zlib=/usr/local/svn/zlib

make make install

