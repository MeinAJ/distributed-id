package com.geega.bsc.id.zk.common.address;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Jun.An3
 * @date 2022/07/11
 */
@Builder
@Data
public class ServerNode implements Serializable, Comparable<ServerNode> {

    private String ip;

    private Integer port;

    private ServerNodeExtension extension;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerNode that = (ServerNode) o;
        if (!Objects.equals(ip, that.getIp())) {
            return false;
        }
        if (!Objects.equals(port, that.getPort())) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ServerNode that) {
        if (this.extension == null) {
            return -1;
        }
        if (this.extension.getClients() == null) {
            return -1;
        }
        if (this.extension.getClients().size() == 0) {
            return -1;
        }
        if (that == null) {
            return 1;
        }
        if (that.extension == null) {
            return 1;
        }
        if (that.extension.getClients() == null) {
            return 1;
        }
        if (that.extension.getClients().size() == 0) {
            return 1;
        }
        boolean flag = this.extension.getClients().size() > that.extension.getClients().size();
        return flag ? 1 : -1;
    }

    public static byte[] getServerNodeDataBytes(String ip, int port) {
        final ServerNode netAddress = ServerNode.builder().ip(ip).port(port).build();
        return JSON.toJSONString(netAddress).getBytes();
    }

}