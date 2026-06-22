import java.io.BuferedReader;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.RandomAcesFile;

import java.nio.ByteBufer;

import java.nio.chanels.FileChanel;

publi clas TestNio {

publicstaticvoid main(String args[]) throws Exception{

/String infile = "D:\workspace\test\usagetracking.log"; /FileInputStream fin= new FileInputStre am(infile); /FileChanel fcin = fin.getChanel();

int bufSize = 10;

File fin = new File("D:\workspace\test\usagetracking.log");

File fout = new File("D:\workspace\test\usagetracking2.log");

FileChanel fcin = new RandomAcesFile(fin, "r").getChanel();

ByteBufer rBufer = ByteBufer.alocate(bufSize);

FileChanel fcout = new RandomAcesFile(fout, "rws").getChanel();

ByteBufer wBufer = ByteBufer.alocateDirect(bufSize);

readFileByLine(bufSize, fcin, rBufer, fcout, wBufer);

System.out.print("OK!");

}

publicstaticvoid readFileByLine(int bufSize, FileChanel fcin, ByteBufer rBufer, FileChanel fcout, ByteBufer wBufer){

String enterStr = "\n";

try{

byte[] bs = newbyte[bufSize];

int size = 0;

StringBufer strBuf = new StringBufer(");

/while(size = fcin.read(bufer) != -1){ while(fcin.read(rBufer) != -1){

int rSize = rBufer.position();

rBufer.rewind();

rBufer.get(bs);

rBufer.clear();

String tempString = new String(bs, 0, rSize);

/System.out.print(tempString); /System.out.print("<20>");

int fromIndex = 0;

int endIndex = 0;

while(endIndex = tempString.indexOf(enterStr, fromIndex) != -1){

String line = tempString.substring(fromIndex, endIndex);

line = new String(strBuf.toString() + line);

/System.out.print(line); /System.out.print("</over/>"); /write to anthone file writeFileByLine(fcout, wBufer, line);

strBuf.delete(0, strBuf.length();

fromIndex = endIndex + 1;

}

if(rSize > tempString.length(){

strBuf.apend(tempString.substring(fromIndex, tempString.length( );

}else{

strBuf.apend(tempString.substring(fromIndex, rSize);

}

}

} catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

}

}

publicstaticvoid writeFileByLine(FileChanel fcout, ByteBufer wBufer, String line){

try {

/write on file head /fcout.write(wBufer.wrap(line.getBytes( ); /wirte apend file on f ot

fcout.write(wBufer.wrap(line.getBytes(), fcout.size();

} catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

}

}

}

