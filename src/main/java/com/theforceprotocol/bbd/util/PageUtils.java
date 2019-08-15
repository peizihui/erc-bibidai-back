package com.theforceprotocol.bbd.util;

import com.theforceprotocol.bbd.domain.entity.BaseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.springframework.data.domain.Sort.Direction.DESC;

public class PageUtils {
    public static Pageable sorted(Pageable pageable) {
        return pageable.getSort() != Sort.unsorted() ? pageable :
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(DESC, BaseEntity.CREATED_DATE));
    }
}
