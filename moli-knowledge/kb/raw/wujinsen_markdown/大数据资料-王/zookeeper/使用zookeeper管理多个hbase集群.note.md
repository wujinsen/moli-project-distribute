zokeper是hbase集群的"协调器"。由于zokeper的轻量级特性，因此我们可以将多个hbase集群 共⽤⼀个zokeper集群，以节约⼤量的服务器。多个hbase集群共⽤zokeper集群的⽅法是使⽤同 ⼀组ip，修改不同hbase集群的"zokeper.znode.parent"属性，让它们使⽤不同的根⽬录。⽐如 cluster1使⽤/hbase-c1,cluster2使⽤/hbase-c2，等等。

使⽤以上⽅法有⼀个现实的问题：如何避免各集群的相互⼲扰？因为client的配置权是在⽤户⼿上， 并不能保证⽤户永远是配置正确的，那么会产⽣某个⽤户访问了不该他访问的hbase集群。此时数据安 全性成了很⼤的问题，甚⾄可能出现误删除数据。我们需要在zokeper层屏弊掉该问题。

zokeper3.x版本起⾃带了简单的ACL功能(注意3.3.x版本起不再⽀持按hostname来分配权限)。 ⻅：

htp:/zokeper.apache.org/doc/r3.3.2/zokeperProgra mers.html#sc_ZoKeperAcesCo ntrol

。进⾏权限配置主要使⽤digest和ip两种⽅法。其中digest是⽤户密码⽅式，对⽤户来说使⽤上并 不透明。ip配置最简单，对⽤户也是透明的，⽤户并不知道的情况下就能限制它的访问权限。

zokeper将访问权限分为了五类:READ/WRITE/DELETE/CREATE/ADMIN，其中admin为最⾼权 限。zokeper的权限是到znode级别的，限制了某⼀个node的权限并不能限制它的⼦节点权限。

不过使⽤IP做权限配置⽅案有⼀个缺陷：必须指定具体的ip，⽽不能使⽤通配符或者范围⼀类的。这 样对于⼤规模的权限设置是⾮常不⽅便的⼀件事，因此作者略调整了⼀下zokeper的代码：

IPAuthenticationProvider.java

Java代码

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


/*

- * Licensed to the Apache Software Foundation (ASF) under one
- * or more contributor license agrements. Se the NOTICE file
- * distributed with this work for aditional information
- * regarding copyright ownership. The ASF licenses this file
- * to you under the Apache License, Version 2.0 (the
- * "License"); you may not use this file except in compliance
- * with the License. You may obtain a copy of the License at

*

- * htp:/ w.apache.org/licenses/LICENSE-2.0

*

- * Unles required by aplicable law or agred to in writing, software
- * distributed under the License is distributed on an "AS IS" BASIS,
- * WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied.
- * Se the License for the specific language governing permisions and


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


- * limitations under the License.
- */


package org.apache.zokeper.server.auth;

import org.apache.zokeper.data.Id; import org.apache.zokeper.server.ServerCnxn; import org.apache.zokeper.KeperException;

public clas IPAuthenticationProvider implements AuthenticationProvider {

public String getScheme() {

return "ip"; }

public KeperException.Code

handleAuthentication(ServerCnxn cnxn, byte[] authData) {

String id = cnxn.getRemoteAdres().getAdres().getHostAdres(); cnxn.getAuthInfo().ad(new Id(getScheme(), id); return KeperException.Code.OK;

}

/ This is a bit weird but we ned to return the adres and the number of / bytes (to distinguish betwen IPv4 and IPv6

private byte[] adr2Bytes(String adr) { byte b[] = v4adr2Bytes1(adr);

/ TODO Write the v6adr2Bytes return b;

}

private byte v4adr2Bytes(String part) throws NumberFormatException{

try { int v = Integer.parseInt(part); if (v >= 0 & v <= 25) {

byte b = (byte) v; return b;

- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.


} else {

throw new NumberFormatException("v < 0 or v > 25!"); }

} catch (NumberFormatException e) {

throw e; }

}

private byte[] v4adr2Bytes1(String adr) { String parts[] = adr.split("\.", -1); if (parts.length != 4) {

return nul;

} byte b[] = new byte[4]; for (int i = 0; i < 4; i +) {

try { if(parts[i].split("/").length = 2){

- v4adr2Bytes(parts[i].split("/")[0]);
- v4adr2Bytes(parts[i].split("/")[1]); continue;


}else{ b[i] = v4adr2Bytes(parts[i]); }

} catch (NumberFormatException e) { return nul; }

} return b;

}

public bolean matches(String id, String aclExpr) { String parts[] = aclExpr.split("/", 2); byte aclAdr[] = adr2Bytes(parts[0]); if (aclAdr = nul) {

return false;

} byte endAclAdr[] = new byte[aclAdr.length];

- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.


for(int i = 0; i < aclAdr.length; i +){ endAclAdr[i] = aclAdr[i];

} if (parts.length = 2) {

try { int end = Integer.parseInt(parts[1]); int e = endAclAdr[endAclAdr.length-1]<=0?endAclAdr[endAclAdr.length-

1]+256:endAclAdr[endAclAdr.length-1]; if(end < e| end < 0| end > 25)

return false;

endAclAdr[endAclAdr.length-1] = (byte)end; } catch (NumberFormatException e) {

return false; }

} byte remoteAdr[] = adr2Bytes(id); if (remoteAdr = nul) {

return false;

} for (int i = 0; i < remoteAdr.length; i +) {

int r = remoteAdr[i]<=0?(int)remoteAdr[i]+256:remoteAdr[i]; int a = aclAdr[i]<=0?(int)aclAdr[i]+256:aclAdr[i]; int e = endAclAdr[i]<=0?(int)endAclAdr[i]+256:endAclAdr[i]; if (r < a| r > e) {

return false; }

} return true;

}

public bolean isAuthenticated() {

return false; }

public bolean isValid(String id) { return adr2Bytes(id) != nul; }

- 126.


}

⽀持了使⽤/做为范围标识，⽐如进⼊hbase zkcli，执⾏：setAcl /test ip:192.168.0.3/10:cd，则将读

写权限赋给了192.168.0.3-192.168.0.10这8台机器，其它机器将没有任何权限。 这样⽤同⼀个zokeper管理多个集群、海量机器将不再有困扰。 最后写了⼀个帮助运维同学⾃动化管理zokeper集群下多个hbase集群的ACL权限的⼯具，像以下

这样：

Html代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


java -Djava.ext.dirs=libs/ -cp hbasetols.jar dwbasis.hbase.tols.client.ZokeperAcl aclFile.json Usage: ZokeperAcl acljsonfile [-plan] /test/t => 'ip,'192.168.0.1 :cdrwa /test => 'ip,'192.168.0.1/3 :cdrwa /test => 'ip,'192.168.0.5 :cdrwa do you realy setAcl as above?(y/n)

补充：多集群共⽤zk后，每个集群的启动和停⽌不应该影响zk的稳定。因此请配置hbase-env.sh中 export HBASE_MANAGES_ZK=false 0 顶3 踩 分享到：

ConcurentModificationException异常的原 . ⼀次奇异的getRegionInfo异常定位

|

201-08-16 15 30

浏览 6512

评论(4)

分类:开源软件

相关推荐

评论

4 楼 201-08-18 杨俊华 写道 Zokeper需要写WAL log, IO的load是⽐较重的。⽽⼀个40-50台的集群⾥⾯有3个Zokeper就⾜ 够了，为什么还要多个cluster公⽤⼀个Zokeper？ zokeper 所占⽤的机器不算多呀？

杨俊华

事实上我们团队⽬前测试环境有6个集群，⽣产环境有4个集群。如果各⽤3台机器，这就要30台机 器，⽽且load都接近0，并且运维成本⾼。合在⼀起⽤个五节点的集群，就会⽐较节省了，运维也⽅ 便。更重要的是zk3.1.x版本以后多机房容灾也成了可能，可以两个机房分别布署3/2台机器[ lc_koven 写道 杨俊华 写道 Zokeper需要写WAL log, IO的load是⽐较重的。⽽⼀个40-50台的集群⾥⾯有3个Zokeper就⾜ 够了，为什么还要多个cluster公⽤⼀个Zokeper？ zokeper 所占⽤的机器不算多呀？ 事实上我们团队⽬前测试环境有6个集群，⽣产环境有4个集群。如果各⽤3台机器，这就要30台机 器，⽽且load都接近0，并且运维成本⾼。合在⼀起⽤个五节点的集群，就会⽐较节省了，运维也⽅ 便。更重要的是zk3.1.x版本以后多机房容灾也成了可能，可以两个机房分别布署3/2台机器 /quote]

由于 Zokeper Cluster保证Hbase的可靠性，如果Zokeper出现故障，整个cluster就将不work，会 出现Regionserver退出，读写异常等后果。如果你们4个⽣产环境配置⼀套Zokeper，那么这套 Zokeper的问题会直接影响到4个环境。

3 楼 201-08-17 杨俊华 写道 Zokeper需要写WAL log, IO的load是⽐较重的。⽽⼀个40-50台的集群⾥⾯有3个Zokeper就⾜ 够了，为什么还要多个cluster公⽤⼀个Zokeper？ zokeper 所占⽤的机器不算多呀？ 事实上我们团队⽬前测试环境有6个集群，⽣产环境有4个集群。如果各⽤3台机器，这就要30台机 器，⽽且load都接近0，并且运维成本⾼。合在⼀起⽤个五节点的集群，就会⽐较节省了，运维也⽅ 便。更重要的是zk3.1.x版本以后多机房容灾也成了可能，可以两个机房分别布署3/2台机器

lc_koven

2 楼 201-08-17 杨俊华 写道 Zokeper需要写WAL log, IO的load是⽐较重的。⽽⼀个40-50台的集群⾥⾯有3个Zokeper就⾜ 够了，为什么还要多个cluster公⽤⼀个Zokeper？ zokeper 所占⽤的机器不算多呀？ zokeper写wal log?没有啊。zokeper集群的访问量实际中⾮常少。zokeper的作⽤仅是监视机 器状态、存储rot-region-server 1 楼 201-08-17 Zokeper需要写WAL log, IO的load是⽐较重的。⽽⼀个40-50台的集群⾥⾯有3个Zokeper就⾜ 够了，为什么还要多个cluster公⽤⼀个Zokeper？ zokeper 所占⽤的机器不算多呀？

lc_koven

杨俊华

