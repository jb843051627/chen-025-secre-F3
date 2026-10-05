package com.fc.v2.service.levelgate;

/**
 * 三闸密级变更的规矩——钉死在此一处。
 *
 * <p>每一关按什么路数点齐、下一关几时才算开得、被压回时抹掉哪一段、
 * 齐讫以后还容不容人补一笔，只认这一处给出的回话。递进来的报文写了第几段、
 * 第几人，进到库里不算数；段位与人数一概以引擎按流水点出来的为凭。
 *
 * @author fuce
 * @date 2026-10-05
 */
public final class LevelGateRule {

    private LevelGateRule() {
    }

    /** 头一闸：承办部门里那一名办事的岗 */
    public static final int GATE_HANDLING = 0;
    /** 次一闸：本机关保密办（两名一同看） */
    public static final int GATE_OFFICE = 1;
    /** 末一闸：上级主管部门对口的那两名 */
    public static final int GATE_SUPERIOR = 2;
    /** 齐闸（三道闸一路放过之后的封存位） */
    public static final int GATE_SEALED = 3;
    /** 闸数 */
    public static final int GATE_COUNT = 3;

    /** 算齐办法：一名即过（头闸只候一名，那一名落字即放过） */
    public static final int MODE_ONE = 0;
    /** 算齐办法：头一个落字那方的说法是定论；后一笔不空写，名数照累、字照留底 */
    public static final int MODE_FIRST_WINS = 1;
    /** 算齐办法：两名一同点头——两句都写「可」才算过；一可一不可挂待议 */
    public static final int MODE_BOTH_AGREE = 2;

    /** 各闸该几名落字 */
    public static final int[] NEED_COUNT = {1, 2, 2};
    /** 各闸按什么算放过 */
    public static final int[] GATE_MODE = {MODE_ONE, MODE_FIRST_WINS, MODE_BOTH_AGREE};
    /** 各闸名目（屏上摆） */
    public static final String[] GATE_NAME = {"承办部门办事岗", "本机关保密办", "上级主管部门"};
    public static final String[] MODE_NAME = {"一名即过", "头名定论·后笔留痕", "两名同可方过"};

    /** 写下的字：不可 */
    public static final int WORD_NO = 0;
    /** 写下的字：可 */
    public static final int WORD_YES = 1;

    /** 本截情形：候签 */
    public static final int ST_WAITING = 0;
    /** 本截情形：挂待议（末闸一可一不可，不往前挪、也不就此了断） */
    public static final int ST_PENDING = 2;
    /** 本截情形：已封住（齐闸，此后一笔进不来、一字改不了、整张也抽不走） */
    public static final int ST_SEALED = 3;

    /** 走法：往上抬 */
    public static final int CHANGE_UP = 0;
    /** 走法：往下压 */
    public static final int CHANGE_DOWN = 1;
    /** 走法：整个解开 */
    public static final int CHANGE_UNSEAL = 2;

    /** 密级：已解密（整个解开之后） */
    public static final int LVL_DECLASSIFIED = 0;
    /** 密级：秘密 */
    public static final int LVL_SECRET = 1;
    /** 密级：机密 */
    public static final int LVL_CONFIDENTIAL = 2;
    /** 密级：绝密 */
    public static final int LVL_TOP_SECRET = 3;

    /**
     * 同一回计算：从哪一级、按这一回走法，挪到哪一级。
     * 册面换栏与叠履历都只从这里取结果，不许另一处由人手填。
     */
    public static int computeToLevel(int changeKind, Integer fromLevel) {
        switch (changeKind) {
            case CHANGE_UP:
                return fromLevel == null ? LVL_SECRET : fromLevel + 1;
            case CHANGE_DOWN:
                return fromLevel - 1;
            case CHANGE_UNSEAL:
                return LVL_DECLASSIFIED;
            default:
                throw new LevelGateException("CHANGE_KIND", "走法不认：只许往上抬／往下压／整个解开");
        }
    }

    /** 起单时先验这一回走法与册面当下密级对不对得上（齐闸时不再有第二种口径）。 */
    public static void checkChangeAllowed(int changeKind, Integer fromLevel) {
        if (changeKind == CHANGE_UP) {
            if (fromLevel != null && fromLevel >= LVL_TOP_SECRET) {
                throw new LevelGateException("CHANGE_KIND", "已是绝密，密级抬不上去了");
            }
        } else if (changeKind == CHANGE_DOWN) {
            if (fromLevel == null || fromLevel <= LVL_SECRET) {
                throw new LevelGateException("CHANGE_KIND", "往下压一级须已在机密以上；要解密请走「整个解开」");
            }
        } else if (changeKind == CHANGE_UNSEAL) {
            if (fromLevel == null || fromLevel == LVL_DECLASSIFIED) {
                throw new LevelGateException("CHANGE_KIND", "册面无密级或已解开，无可解");
            }
        } else {
            throw new LevelGateException("CHANGE_KIND", "走法不认：只许往上抬／往下压／整个解开");
        }
    }
}
