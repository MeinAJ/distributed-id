package com.geega.bsc.id.server.general;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 12:12 上午
 */
@Slf4j
public class RandomMachineIdGeneral extends BaseMachineIdGeneral {
    @Override
    protected Long generalId() {
        log.info("使用随机算法生成machine id");
        return (long) new Random().nextInt(1024);
    }
}
