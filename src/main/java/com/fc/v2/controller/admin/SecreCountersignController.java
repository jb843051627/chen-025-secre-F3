package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.common.exception.file.CountersignRuleException;
import com.fc.v2.model.auto.TSecreCountersign;
import com.fc.v2.model.auto.TSecreLevelLog;
import com.fc.v2.service.ITSecreCountersignService;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 密级变更三道闸会签 Controller（countersign 新名目）。
 *
 * 入水口只有两个：由载体行起单（open）、一支笔落字（sign）。
 * 报文里没有第几闸、第几轮、已签几名人手填的格子——停在哪、到了几名，全由 service
 * 顺着受理簿点；落字人只认登录会话本人，不许报文自报。没有改旧笔、追补签、删单的口。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Api(value = "密级变更三道闸会签")
@Controller
@RequestMapping("/secreCountersign")
public class SecreCountersignController extends BaseController {

    @Autowired
    private ITSecreCountersignService countersignService;

    @Log(title = "会签单按闸回显", action = "view")
    @ApiOperation(value = "按闸分截回显", notes = "该几名/怎么算过/到了几名/停在哪一截，全是系统点的回话")
    @GetMapping("/view")
    @RequiresPermissions("secre:countersign:view")
    @ResponseBody
    public AjaxResult view(@RequestParam("id") Long id) {
        return retobject(200, countersignService.view(id));
    }

    @Log(title = "会签台账", action = "list")
    @ApiOperation(value = "会签台账", notes = "open=1 只列未齐闸，缺省连齐闸封住的一并列")
    @GetMapping("/list")
    @RequiresPermissions("secre:countersign:list")
    @ResponseBody
    public AjaxResult list(@RequestParam(value = "open", required = false, defaultValue = "0") Integer open) {
        List<TSecreCountersign> rows = countersignService.list(open != null && open == 1);
        return retobject(200, rows);
    }

    @Log(title = "密级会签由载体行起单", action = "open")
    @ApiOperation(value = "由载体册面那一行起单", notes = "同一件载体未齐闸的单只带得出一张")
    @PostMapping("/open")
    @RequiresPermissions("secre:countersign:open")
    @ResponseBody
    public AjaxResult open(@RequestParam("carrierId") Long carrierId,
                           @RequestParam("changeKind") Integer changeKind,
                           @RequestParam(value = "levelTo", required = false) Integer levelTo,
                           @RequestParam(value = "remark", required = false) String remark) {
        TSecreCountersign cs = countersignService.openForCarrier(carrierId, changeKind, levelTo, remark);
        return retobject(200, countersignService.view(cs.getId()));
    }

    @Log(title = "密级会签落字（唯一入口）", action = "sign")
    @ApiOperation(value = "落字", notes = "落在哪一闸由系统点；落字人取登录本人，报文不报段位不报人数")
    @PostMapping("/sign")
    @RequiresPermissions("secre:countersign:sign")
    @ResponseBody
    public AjaxResult sign(@RequestParam("id") Long id,
                           @RequestParam("agree") Boolean agree,
                           @RequestParam(value = "remark", required = false) String remark) {
        if (agree == null) {
            throw new CountersignRuleException("落字只认「可」或「不可」，空着不算");
        }
        // 三道闸各是各的人、谁也替不了谁：接笔闸点出来是第几闸，就验那一闸的落字权限。
        Integer gate = countersignService.view(id).getCurrentNode();
        if (gate != null) {
            SecurityUtils.getSubject().checkPermission("secre:countersign:gate" + gate);
        }
        TSecreCountersign cs = countersignService.sign(id, ShiroUtils.getLoginName(), agree, remark);
        return retobject(200, countersignService.view(cs.getId()));
    }

    @ApiOperation(value = "在办张数", notes = "随起单加一、齐闸减一，与逐闸点算同一回账")
    @GetMapping("/openCount")
    @RequiresPermissions("secre:countersign:list")
    @ResponseBody
    public AjaxResult openCount() {
        return retobject(200, countersignService.openCount());
    }

    @Log(title = "会签回数核对", action = "reconcile")
    @ApiOperation(value = "回数核对", notes = "从末闸回数回头闸，与受理簿一笔一笔对；id 缺省全盘点名")
    @GetMapping("/reconcile")
    @RequiresPermissions("secre:countersign:recon")
    @ResponseBody
    public AjaxResult reconcile(@RequestParam(value = "id", required = false) Long id) {
        return retobject(200, id == null ? countersignService.reconcileAll() : countersignService.reconcile(id));
    }

    @ApiOperation(value = "载体密级变更履历", notes = "最新在头里，一段一段倒着捋")
    @GetMapping("/history")
    @RequiresPermissions("secre:countersign:view")
    @ResponseBody
    public AjaxResult history(@RequestParam("carrierId") Long carrierId) {
        List<TSecreLevelLog> logs = countersignService.historyOfCarrier(carrierId);
        return retobject(200, logs);
    }
}
