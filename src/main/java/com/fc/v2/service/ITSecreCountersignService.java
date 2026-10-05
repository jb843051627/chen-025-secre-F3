package com.fc.v2.service;

import com.fc.v2.model.auto.TSecreCountersign;
import com.fc.v2.model.auto.TSecreLevelLog;
import com.fc.v2.model.custom.countersign.CountersignView;
import com.fc.v2.model.custom.countersign.ReconReport;

import java.util.List;

/**
 * 密级变更三道闸会签 Service（countersign 新名目）。
 *
 * 只此一处给出回话：段位、该几名、到了几名、齐没齐、在办张数、回数核对，全由本处
 * 顺着受理簿逐笔点出。没有另起单（起单只由载体册面那一行带出）、改旧笔、追补签入口，
 * 也没有删除/抽走入口。旧 approval-chain（TSecreLevelBillService 的 approve/reject/
 * rollback）形状锁住，与本处互不相干。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface ITSecreCountersignService {

    /** 起单：只能由册面上载体那一行带出；同一件载体有未齐闸的单时挡回。落定头闸前停在头闸。 */
    TSecreCountersign openForCarrier(Long carrierId, Integer changeKind, Integer levelTo, String remark);

    /**
     * 落字——唯一一支笔的入口。
     * 停在哪一闸、第几轮、第几笔、到了几名，一概由本处点，参数里没有这些格子；
     * 越闸递来、本闸已满、本人本轮已落、单已封住/挂待议的，一律不作数（抛 CountersignRuleException）。
     *
     * @param csId    会签单
     * @param signer  落字人
     * @param agree   true「可」 false「不可」
     * @param remark  落字原话（留底）
     */
    TSecreCountersign sign(Long csId, String signer, boolean agree, String remark);

    /** 按闸分截回显：四项与逐笔留痕全是点算结果，无一格给人填。 */
    CountersignView view(Long csId);

    /** 台账：openOnly=true 只点未齐闸的（在签/待议/压回重走），false 连齐闸封住的一并列出。 */
    List<TSecreCountersign> list(boolean openOnly);

    /** 在办张数：随起单加一、随齐闸减一，和逐闸点算是同一回账。 */
    int openCount();

    /** 单张核对：从末闸回数回头闸，闸数与受理簿逐笔对齐；对得齐才 ok。 */
    ReconReport reconcile(Long csId);

    /** 全盘核对：把在办各张逐一点名，凡册面停留闸/情形/轮次与受理簿点出来的不一致，逐条报出。 */
    ReconReport reconcileAll();

    /** 载体那一行的密级变更履历：最新一遭在头里，一段一段倒着捋。 */
    List<TSecreLevelLog> historyOfCarrier(Long carrierId);
}
