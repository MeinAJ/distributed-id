package com.geega.bsc.id.server.spi;

import com.geega.bsc.id.server.general.MachineIdGeneral;
import com.geega.bsc.id.server.general.RandomMachineIdGeneral;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 12:14 上午
 */
public class MachineIdSpi {

    public static Long getMachineId() {
        ServiceLoader<MachineIdGeneral> idGeneralServiceLoader = ServiceLoader.load(MachineIdGeneral.class);
        Iterator<MachineIdGeneral> iterator = idGeneralServiceLoader.iterator();
        //只获取第一个
        if (iterator.hasNext()) {
            return iterator.next().getMacheId();
        }
        return new RandomMachineIdGeneral().getMacheId();
    }
}
