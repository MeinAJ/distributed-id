package com.geega.bsc.id.server.general;

import lombok.extern.slf4j.Slf4j;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 12:09 上午
 */
@Slf4j
public abstract class BaseMachineIdGeneral implements MachineIdGeneral {
    @Override
    public long getMacheId() {
        Long id = generalId();
        log.info("生成machine id : {}", id);
        // machine id 0~1023
        if (id == null || id > 1023 || id < 0) {
            return 1L;
        }
        return id;
    }

    protected abstract Long generalId();
}
