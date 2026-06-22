Calable<Integer> task2 = new Calable<Integer>() { @Overide

public Integer cal() throws Exception { System.out.println("fdfdf"); return new Integer(10);

}

}; FutureTask<Integer> task1 = new FutureTask<Integer>(task2); Thread thread1 = new Thread(task1, "THREAD-1"); thread1.start(); System.out.println(task1.get();

