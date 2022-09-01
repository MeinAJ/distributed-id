package com.geega.bsc.id.client.cache;

import lombok.Builder;
import lombok.Data;

/**
 * @author Jun.An3
 * @date 2022/07/25
 */
@Data
@Builder
public class CacheConfig {

    /**
     * eg:20
     */
    Integer capacity = 300;

    /**
     * eg:10
     */
    Integer trigger = 220;

}
