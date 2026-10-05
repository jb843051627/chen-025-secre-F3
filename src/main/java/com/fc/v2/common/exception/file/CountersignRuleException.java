package com.fc.v2.common.exception.file;

/**
 * 会签六则被挡：越闸落字、本闸已满、本人重笔、单已封住/挂待议、借通用口手改密级等。
 * 不是系统出错，是规矩不作数；原话报给屏上，便于点名为哪一桩。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class CountersignRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CountersignRuleException(String message) {
        super(message);
    }
}
