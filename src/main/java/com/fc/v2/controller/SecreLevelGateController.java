package com.fc.v2.controller;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.service.levelgate.LevelGateEngine;
import com.fc.v2.service.levelgate.LevelGateException;
import com.fc.v2.service.levelgate.LevelGateRule;
import com.fc.v2.service.levelgate.LevelGateView;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 三闸密级变更。
 *
 * <p>单面沿用现行那张，不另起式样；对外只有三张口：
 * 由册面行带出一张单、往停截递一笔字（可／不可都走这一笔，不另开 approve/reject/rollback）、
 * 点一张单回来看。另起单、改旧笔、追补签这三类暗门一张都不留。
 * 屏上已签数只是回显，没有一栏留给人手填。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Api(tags = "三闸密级变更")
@Controller
@RequestMapping("/secre/levelGate")
public class SecreLevelGateController extends BaseController {

    @Autowired
    private LevelGateEngine levelGateEngine;

    /**
     * 起单：只由册面上那一行带出来（同一件载体、同一回变更只带得出一张）。
     *
     * @param carrierId  册面行
     * @param changeKind 0往上抬 1往下压 2整个解开
     */
    @ApiOperation(value = "由册面行带出一张三闸单", notes = "changeKind 0往上抬 1往下压 2整个解开")
    @PostMapping("/open")
    @ResponseBody
    public AjaxResult open(@RequestParam("carrierId") Long carrierId,
                           @RequestParam("changeKind") Integer changeKind,
                           @RequestParam(value = "remark", required = false) String remark) {
        try {
            LevelGateView view = levelGateEngine.open(carrierId,
                    changeKind == null ? -1 : changeKind, remark);
            return AjaxResult.successData(200, view);
        } catch (LevelGateException e) {
            return AjaxResult.error(500, e.getMessage());
        }
    }

    /**
     * 递笔——一支笔只此一处入口。单停在哪一截只认那一截，报文里自称第几段不算数；
     * 越过次一闸直接递到末闸的那一笔不作数。写「不可」即由本闸压回，不另开退回口。
     *
     * @param billId   哪张单
     * @param signerNo 谁落字（不传则取登录人）
     * @param verdict  1可 0不可
     * @param word     原话留底
     */
    @ApiOperation(value = "往停截递一笔字（唯一入口）", notes = "verdict 1可 0不可；末闸一可一不可挂待议")
    @PostMapping("/sign")
    @ResponseBody
    public AjaxResult sign(@RequestParam("billId") Long billId,
                           @RequestParam(value = "signerNo", required = false) String signerNo,
                           @RequestParam("verdict") Integer verdict,
                           @RequestParam(value = "word", required = false) String word) {
        String who = (signerNo == null || signerNo.trim().isEmpty())
                ? ShiroUtils.getLoginName() : signerNo.trim();
        try {
            LevelGateView view = levelGateEngine.sign(billId, who,
                    verdict == null ? -1 : verdict, word);
            return AjaxResult.successData(200, view);
        } catch (LevelGateException e) {
            return AjaxResult.error(500, e.getMessage());
        }
    }

    /** 点一张单回来看：每闸该几名、按什么算放过、当下到了几名、整张停在哪一截。 */
    @ApiOperation(value = "三闸单回显", notes = "已签数为系统点数回显，不可手填")
    @GetMapping("/view/{id}")
    @ResponseBody
    public AjaxResult view(@PathVariable("id") Long id) {
        try {
            return AjaxResult.successData(200, levelGateEngine.view(id));
        } catch (LevelGateException e) {
            return AjaxResult.error(500, e.getMessage());
        }
    }

    /** 屏上那个数：在办张数随起单加一、随齐闸减一。 */
    @ApiOperation(value = "在办三闸单张数", notes = "与逐闸点数同一回算的账")
    @GetMapping("/openCount")
    @ResponseBody
    public AjaxResult openCount() {
        return AjaxResult.successData(200, levelGateEngine.openCount());
    }

    /** 事后对账：从末一闸回数回头一闸，与受理簿一笔一笔轧；对不齐点名报出。 */
    @ApiOperation(value = "对账", notes = "返回对不齐的条目（空为账齐）")
    @GetMapping("/reconcile")
    @ResponseBody
    public AjaxResult reconcile() {
        List<String> issues = levelGateEngine.reconcile();
        return AjaxResult.successData(200, issues);
    }

    /** 各闸规矩的回显（该几名、什么路数）——只认引擎这一处给的回话。 */
    @ApiOperation(value = "三闸规矩", notes = "应签名数与算齐办法只从此处取")
    @GetMapping("/rules")
    @ResponseBody
    public AjaxResult rules() {
        java.util.List<Object> gates = new java.util.ArrayList<>();
        for (int g = 0; g < LevelGateRule.GATE_COUNT; g++) {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("gateNo", g);
            m.put("gateName", LevelGateRule.GATE_NAME[g]);
            m.put("needCount", LevelGateRule.NEED_COUNT[g]);
            m.put("modeName", LevelGateRule.MODE_NAME[LevelGateRule.GATE_MODE[g]]);
            gates.add(m);
        }
        return AjaxResult.successData(200, gates);
    }
}
