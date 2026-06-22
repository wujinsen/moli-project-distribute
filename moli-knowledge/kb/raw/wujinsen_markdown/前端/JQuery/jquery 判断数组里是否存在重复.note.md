var ary = new Array("111","ff","222","aa","222"); alert(mm(ary)) // 验证重复元素，有重复返回true；否则返回false function mm(a) {

return /(\x0f[^\x0f]+)\x0f[\s\S]*\1/.test("\x0f"+a.join("\x0f\x0f") +"\x0f"); }

// ⽅法⼆，通过数组排序，⽐较临近元素，可指出重复的元素 var ary = newArray("111","22","33","111","22"); var nary = ary.sort(); for(var i = 0; i < nary.length - 1; i++) {

if (nary[i] == nary[i+1]) {

alert("重复内容：" + nary[i]); }

}

// ⽅法三，通过字符串查找 var ary = newArray("111","22","33","111","22"); var s = ary.join(",") +","; for(var i = 0; i < ary.length; i++) {

if(s.replace(ary[i] + ",", "").indexOf(ary[i] +",") > -1) {

alert("重复内容：" + ary[i]); }

}

// ⽅法四，通过哈希 var ary = newArray("111","22","33","111","22"); alert(isRepeat(ary)); // 验证重复元素，有重复返回true；否则返回false function isRepeat(arr) {

var hash = {}; for(var i in arr) {

if(hash[arr[i]]) {

return true;

} // 不存在该元素，则赋值为true，可以赋任意值，相应的修改if判断条件即可 hash[arr[i]] = true;

}

return false; }

