package com.theorydance.esoperator.demo;

import java.util.List;
import java.util.Map;

import com.theorydance.esoperator.plugin.EsProxy;
import com.theorydance.esoperator.utils.EsUtils;

public class QueryDataDemo {

	public static void main(String[] args) {
		EsUtils.setEsHost("http://127.0.0.1:9200/");
		TestMapper mapper = EsProxy.getProxy(TestMapper.class);
		List<Map<String, Object>> list = mapper.getDataList("测试", 20);
		for (Map<String, Object> map : list) {
			System.out.println(map);
		}
	}

}
