package com.fc.v2.mapper.auto;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fc.v2.model.auto.TSecreCountersign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 密级变更三道闸会签单数据层
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface TSecreCountersignMapper extends BaseMapper<TSecreCountersign> {

    /**
     * 行锁取单：一笔落字从头到尾对住同一行，同瞬两笔也只能一前一后进来。
     */
    @Select("select * from t_secre_countersign where id = #{id} and del_flag = 0 for update")
    TSecreCountersign selectByIdForUpdate(@Param("id") Long id);
}
