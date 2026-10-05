package com.fc.v2.service.levelgate;

import java.util.function.LongSupplier;

/**
 * 落字那一刻的取时口。实现里接系统钟；测试可另给，便于把「同瞬两名」钉死。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TimeSource extends LongSupplier {

    /** 系统钟（毫秒） */
    TimeSource SYSTEM = System::currentTimeMillis;
}
