package com.theorydance.esoperator;

import java.lang.reflect.Method;

import com.theorydance.esoperator.demo.TestMapper;
import com.theorydance.esoperator.plugin.EsProxy;

public class TestMain {

	public static void main(String[] args) throws Exception{
		TestMapper testMapper = EsProxy.getProxy(TestMapper.class);
		try {
			testMapper.getDataList("", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			testMapper.getDataList("", 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
