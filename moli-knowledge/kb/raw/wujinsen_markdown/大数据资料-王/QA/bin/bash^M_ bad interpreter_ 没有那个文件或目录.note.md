运⾏脚本时出现了这样⼀个错误，打开之后并没有找到所谓的^M，查了之后才知道原来是⽂件格式的 问题，也就是linux和windows之间的不完全兼容。。。 具体细节不管，如果验证：

vim test.sh :set ff? 如果出现fileforma＝dos那么就基本可以确定是这个问题了。 :set fileformat=unix :wq

OK了。。。。。。。

