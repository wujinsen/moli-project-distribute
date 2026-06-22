/**

- * 抓取服务器状态

- * @param ip

- */


public void fetchServerStatus(ﬁnal String i p ) {

try {

sshTemplate.execute(i p , new SSHCallback() { public Result call(SSHSession s e s s i o n ) { //尝试收集服务器运⾏状况 collectServerStatus(ip, s e s s i o n ); //启动nmon收集服务器运⾏状况 OSInfo info = nmonService.start(ip, s e s s i o n ); saveServerStatus(ip, info); return null;

} });

} catch (Exception e ) {

l o g g e r .error("fetchServerStatus "+i p +" err", e ); }

}

//异步执⾏任务 asyncMonitorMachineStats(){

asyncService.submitFuture(AsyncThreadPolFactory.MACHINE_POL, new KeyCalable<Bolean>(key) {

/查询主机静态信息(数据库) monitorMachineStats(hostId, ip);

监控机器的状态 monitorMachineStats(final long hostId, final String ip)

//异步执⾏任务

public void asyncFetchServerStatus(ﬁnal String i p ) { String key = "collect-server-"+i p ; asyncService.submitFuture(AsyncThreadPoolFactory.M A C H I N E _P O O L , new

KeyCallable<Boolean>(key) { public Boolean execute() {

try { fetchServerStatus(ip); return true;

} catch (Exception e ) { l o g g e r .error(e .getMessage(), e ); return false;

} }

}); }

public boolean submitFuture(String t h r e a d P o o l K e y , KeyCallable<?> c a l l a b l e ) {

try { Future<?> future = getExecutorService(t h r e a d P o o l K e y ).submit(c a l l a b l e ); //忽略queue溢出 futureQueue.put(new KeyFuture(c a l l a b l e .getKey(), future)); return true;

} catch (Exception e ) { logger.error(e .getMessage() + c a l l a b l e .getKey(), e ); return false;

} }

