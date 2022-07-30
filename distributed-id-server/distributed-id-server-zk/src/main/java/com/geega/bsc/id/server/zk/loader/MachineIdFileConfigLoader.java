package com.geega.bsc.id.server.zk.loader;

import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.ResourcesUtil;
import com.geega.bsc.id.server.zk.config.MachineIdConfigConst;
import com.geega.bsc.id.server.zk.config.MachineIdFileConfig;

import java.util.Properties;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 6:02 下午
 */
public class MachineIdFileConfigLoader {

    private static MachineIdFileConfig machineIdFileConfig;

    public static synchronized MachineIdFileConfig loadMachineIdFileConfig() {
        if (machineIdFileConfig != null) {
            return machineIdFileConfig;
        }
        try {
            machineIdFileConfig = new MachineIdFileConfig();

            Properties properties = ResourcesUtil.getProperties("/application.properties");

            machineIdFileConfig.setRoot(properties.getProperty(MachineIdConfigConst.MACHINE_ID_FILE_ROOT, "."));
            machineIdFileConfig.setName(properties.getProperty(MachineIdConfigConst.MACHINE_ID_FILE_NAME, "machineId.data"));
            return machineIdFileConfig;
        } catch (Exception e) {
            machineIdFileConfig = null;
            throw new DistributedIdException("读取machine id配置文件异常", e);
        }
    }
}
