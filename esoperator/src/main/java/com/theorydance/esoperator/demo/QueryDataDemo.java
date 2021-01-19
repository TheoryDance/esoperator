package com.theorydance.esoperator.demo;

import java.util.List;
import java.util.Map;

import com.theorydance.esoperator.plugin.EsProxy;
import com.theorydance.esoperator.utils.EsUtils;

/**
 * 查询操作类
 */
public class QueryDataDemo {

	public static void main(String[] args) {
		// 指定elasticsearch提供服务的ip地址和端口，如果不设置，就会采用默认值"http://127.0.0.1:9200/" 
		EsUtils.setEsHost("http://127.0.0.1:9200/");
		// 使用代理类，获取接口对象
		TestMapper mapper = EsProxy.getProxy(TestMapper.class);
		// 传递参数，查询相关数据，自动解析结果放入到结果集中
		List<Map<String, Object>> list = mapper.getDataList("测试", 20);
		for (Map<String, Object> map : list) {
			System.out.println(map);
		}
	}
}
