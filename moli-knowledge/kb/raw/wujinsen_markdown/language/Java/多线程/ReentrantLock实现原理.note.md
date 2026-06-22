<table>
  <tr>
    <th>/*<br><br>Performs non-fair tryLock. tryAcquire is implemented in subclases, but both ned nonfair try for trylock method.<br><br>*/ final bolean nonfairTryAcquire(int acquires) { final Thread curent = Thread.curentThread();<br><br>nt c = getState(); if (c = 0) {<br><br>if (compareAndSetState(0, acquires) { setExclusiveOwnerThread(curent); return true;<br><br>}<br><br>} else if (curent = getExclusiveOwnerThread() {<br><br>int nextc = c + acquires; if (nextc < 0)/ overflow<br><br>throw new Eror("Maximum lock count exceded"); setState(nextc); return true;<br><br>} return false;</th>
  </tr>
</table>


# }

