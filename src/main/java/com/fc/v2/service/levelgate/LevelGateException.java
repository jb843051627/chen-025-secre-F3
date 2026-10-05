package com.fc.v2.service.levelgate;

/**
 * 三闸规程上不放行时的点名回话（哪一处、为什么）。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class LevelGateException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 回话名目，例如 GATE_NOT_OPEN／ALREADY_SEALED／DUPLICATE_SIGN */
    private final String code;

    public LevelGateException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
