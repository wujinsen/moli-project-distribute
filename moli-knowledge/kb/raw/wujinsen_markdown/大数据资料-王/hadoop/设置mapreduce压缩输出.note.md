map压缩输出 conf.setBolean("mapred.compres.map.out", true);/设置map输出压缩

conf.setClas(Job.MAP_OUTPUT_COMPRES_CODEC, GzipCodec.clas, CompresionCodec.clas s);

reduce压缩输出 conf.setBolean("mapred.output.compres", true);/设置输出压缩

