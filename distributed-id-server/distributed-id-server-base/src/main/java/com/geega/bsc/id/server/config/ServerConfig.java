package com.geega.bsc.id.server.config;

import lombok.Data;

/**
 * @author Jun.An3
 * @date 2022/07/14
 */
@Data
public class ServerConfig {

    private String ip;

    private Integer port;

    private Integer processor;

    private Long idDatacenter;

}
