//计算后n分钟的时间 function addMinutes(date,minutes)

{

minutes=parseInt(minutes); var interTimes=minutes*60*1000; interTimes=parseInt(interTimes); return new Date(Date.parse(date)+interTimes);

}

//计算前n分钟的时间 function desendMinutes(date,minutes) {

minutes=parseInt(minutes); var interTimes=minutes*60*1000; interTimes=parseInt(interTimes); return new Date(Date.parse(date)-interTimes);

}

// 计算两个⽇期的间隔天数 function DateDiff(sDate1, sDate2)

{ //sDate1和sDate2是2002-12-18格式 var aDate, oDate1, oDate2, iDays

- aDate = sDate1.split("-")

- oDate1 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]) //转换为12-18-2002格式

aDate = sDate2.split("-")

- oDate2 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]) iDays = parseInt(Math.abs(oDate1 - oDate2) / 1000 / 60 / 60 /24) //把相差的毫秒数转换为




天数

return iDays }

//格式化时间为yyyy/mm/dd hh:mm:ss形式 function JFormatDatepmz(strDate)

{

var strPdate=strDate; var iYear,iMonth,iDate,ihour,iminute,sec,sDate; iYear=strPdate.getYear();

iMonth=strPdate.getMonth()+1; iDate=strPdate.getDate(); ihour=strPdate.getHours(); iminute=strPdate.getMinutes(); sec = strPdate.getSeconds(); sDate=String(iYear);

if(iMonth<10) {

sDate = sDate+"/0"+ String(iMonth) ;

} else {

sDate = sDate+"/"+String(iMonth) ;

} if (iDate < 10) {

sDate = sDate+"/0"+String(iDate) ;

} else {

sDate = sDate+"/"+String(iDate) ;

} if(ihour<10) {

sDate = sDate+" "+"0"+ String(ihour) ;

} else {

sDate = sDate+" "+String(ihour) ;

} if(iminute<10) {

sDate = sDate+":"+"0"+ String(iminute) ;

} else {

sDate = sDate+":"+String(iminute) ;

} if(sec<10) {

sec = "0"+ String(sec);

} sDate = sDate+":"+sec; return sDate;

}

