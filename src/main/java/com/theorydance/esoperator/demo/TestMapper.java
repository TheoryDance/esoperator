package com.theorydance.esoperator.demo;

import java.util.List;
import java.util.Map;

import com.theorydance.esoperator.plugin.Esparam;

public interface TestMapper {
	
	List<Map<String, Object>> getDataList(@Esparam("remark") String remark, @Esparam("size") int pagesize);
	
}
