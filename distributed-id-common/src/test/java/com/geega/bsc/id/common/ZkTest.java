/*
 * Copyright (c) 2019, ABB and/or its affiliates. All rights reserved.
 * ABB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.geega.bsc.id.common;

/**
 * ZkTest
 *
 * @author Jun.An3
 * @date 2022/06/30
 */
public class ZkTest {

//    public static void main(String[] args) throws Exception {
//        ZookeeperFactory zkFactory = new ZookeeperFactory();
//        final CuratorFramework instance = zkFactory.instance();
//        //创建服务端目录
//        String s = instance.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/distributed/id/server", "{\"ip\":\"127.0.0.1\",\"port\":\"10000\"}".getBytes());
//        System.out.println(s);
//        s = instance.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/distributed/id/server", "{\"ip\":\"127.0.0.1\",\"port\":\"10001\"}".getBytes());
//        System.out.println(s);
//        //获取服务端目录下可用的ip+port
//        List<String> children = instance.getChildren().forPath("/distributed/id");
//        for (String child : children) {
//            byte[] bytes = instance.getData().forPath("/distributed/id/" + child);
//            System.out.println(new String(bytes));
//        }
//        //向zk创建临时顺序节点后,拿到顺序节点的值
//
//    }

}
