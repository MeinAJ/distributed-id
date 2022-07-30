package com.geega.bsc.id.zk.common.address;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Jun.An3
 * @date 2022/07/11
 */
@Builder
@Data
public class ServerNodeExtension implements Serializable {

    private Set<String> clients;

}