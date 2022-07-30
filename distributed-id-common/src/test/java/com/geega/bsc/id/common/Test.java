package com.geega.bsc.id.common;

public class Test {

//    private LinkedBlockingQueue<Long> current = new LinkedBlockingQueue<>(20);
//
//    private SnowFlake snowFlake = new SnowFlake(1, 1);
//
//
//    public void start() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        current.put(snowFlake.nextId());
//                        System.out.println("put 一个数据，当前size=" + current.size());
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }
//
//
//    public Long get() {
//        try {
//            return current.take();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public List<Long> getIds(Long number) {
//        List<Long> ids = new ArrayList<>();
//        for (int i = 0; i < number; i++) {
//            ids.add(get());
//        }
//        return ids;
//    }
//
//    public static void main(String[] args) {
//        Test test = new Test();
//        test.start();
//        for (int i=0; i < 10; i++) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//                        test.getIds((long) new Random().nextInt(20));
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
//        }
//    }
}
