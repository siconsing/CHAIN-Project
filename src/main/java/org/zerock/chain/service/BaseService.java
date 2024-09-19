package org.zerock.chain.service;

import java.util.List;
import java.util.function.Function;

public abstract class BaseService<T> {

    protected abstract List<T> getAllItemsByEmpNo(Long empNo);

    public List<T> getItemsByEmpNo(Long empNo, Function<Long, List<T>> specificServiceFunction) {
        if (empNo != null) {
            return specificServiceFunction.apply(empNo);
        }
        return List.of();  // 빈 리스트 반환
    }
}
