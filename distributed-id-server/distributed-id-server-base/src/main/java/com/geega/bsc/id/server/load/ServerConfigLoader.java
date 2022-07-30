package com.geega.bsc.id.server.load;

import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.IpUtil;
import com.geega.bsc.id.common.utils.ResourcesUtil;
import com.geega.bsc.id.common.utils.StringUtil;
import com.geega.bsc.id.server.config.ConfigConst;
import com.geega.bsc.id.server.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 5:25 下午
 */
@Slf4j
public class ServerConfigLoader {

    private static ServerConfig serverConfig;

    public static synchronized ServerConfig loadServerConfig() {
        if (serverConfig != null) {
            return serverConfig;
        }
        try {
            serverConfig = new ServerConfig();

            Properties properties = ResourcesUtil.getProperties("/application.properties");
            String ip = properties.getProperty(ConfigConst.BIND_IP);
            if (ip == null || "".equals(ip)) {
                ip = IpUtil.getIp();
                log.info("本机ip:{}", ip);
            }
            assert !StringUtil.isEmpty(ip);

            String port = System.getProperty(ConfigConst.BIND_PORT);
            if (port == null || "".equals(port)) {
                port = properties.getProperty(ConfigConst.BIND_PORT);
            }
            assert !StringUtil.isEmpty(port);
            assert !StringUtil.isEmpty(properties.getProperty(ConfigConst.ID_WORK_ID_ROOT));
            assert !StringUtil.isEmpty(properties.getProperty(ConfigConst.ID_WORK_ID_FILE));
            serverConfig.setIp(ip);
            serverConfig.setPort(Integer.valueOf(port));
            serverConfig.setProcessor(Integer.valueOf(properties.getProperty(ConfigConst.NIO_PROCESSOR, "3")));
            serverConfig.setIdDatacenter(Long.valueOf(properties.getProperty(ConfigConst.ID_DATACENTER, "1")));

            return serverConfig;
        } catch (Exception e) {
            serverConfig = null;
            throw new DistributedIdException("读取配置文件异常", e);
        }
    }
}
