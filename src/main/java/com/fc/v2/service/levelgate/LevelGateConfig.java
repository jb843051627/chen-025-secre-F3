package com.fc.v2.service.levelgate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 三闸引擎装配：账本一口、系统钟一口。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Configuration
public class LevelGateConfig {

    @Bean
    public LevelGateEngine levelGateEngine(LevelGateRepository repository) {
        return new LevelGateEngine(repository, TimeSource.SYSTEM);
    }
}
