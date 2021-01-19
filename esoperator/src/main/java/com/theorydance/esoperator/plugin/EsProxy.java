package com.theorydance.esoperator.plugin;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import com.theorydance.esoperator.utils.EsUtils;

public class EsProxy implements InvocationHandler{

	public final static Map<String, Map<String,String>> dslMap = new ConcurrentHashMap<>();

	public static <T> T getProxy(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T proxyInstance = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new EsProxy());
        return proxyInstance;
	}

	private boolean contain(Class<?>[] array, Class<?> target) {
		for (Class<?> clazz : array) {
			if(clazz.toString().equals(target.toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 定义变量，用于存放传递的参数
		Map<String,Object> paramterMap = new HashMap<>();

		if(args!=null && args.length>0) {

			if(args.length==1 && contain(args[0].getClass().getInterfaces(), Map.class)) {
				Map<? extends String,? extends Object> mm = (Map)args[0];
				paramterMap.putAll(mm);
			}else {
				// 获取参数的注解名称
		        List<Esparam> paramterAnnos = new ArrayList<>();
		        Annotation[][] lll = method.getParameterAnnotations();
		        for (Annotation[] annotations : lll) {
					for (Annotation annotation : annotations) {
						paramterAnnos.add((Esparam)annotation);
					}
				}

		        // 将传递的参数使用Map进行保存
		        Type[] paramterTypes = method.getGenericParameterTypes();
		        for(int i=0;i<paramterTypes.length;i++) {
		        	String key = paramterAnnos.get(i).value();
		        	Object value = args[i];
		        	paramterMap.put(key, value);
		        }
			}
		}

        // 先判断是否已经解析过，如果已经解析，则不需要进行重复解析
        String methodName = method.getName();
        Map<String,String> dslDefine = dslMap.get(methodName); // 从xml中得到该方法对应的dsl定义
        if(dslDefine == null) {
        	 // 解析该方法对应的xml文件
            SAXParserFactory factory = SAXParserFactory.newInstance();//1.或去SAXParserFactory实例
            SAXParser saxParser = factory.newSAXParser();//2.获取SAXparser实例
            EsDSLPraseHandel handel = new EsDSLPraseHandel();//创建Handel对象
            // 找到该方法对应的xml文件，要求xml文件与定义的方法在同一个目录中，且文件名相同，方法名相同
            InputStream xmlFile = EsProxy.class.getResourceAsStream("/"+method.getDeclaringClass().getName().replace(".", "/")+".xml");
            saxParser.parse(xmlFile, handel);
            dslDefine = dslMap.get(methodName); // 从xml中得到该方法对应的dsl定义
        }

        String dsl = dslDefine.get("dsl");
        String esIndex = dslDefine.get("esIndex");
        esIndex = replaceVar(esIndex, paramterMap);
        String parseResult = dslDefine.get("parseResult");
		parseResult = replaceVar(parseResult, paramterMap);
		List<Map<String, Object>> list = EsUtils.getEsAggsData(esIndex, dsl, parseResult, paramterMap, paramterMap);
        return list;
	}

	/**
	 * 将字符串中的${var_name}替换为变量var_name的值
	 */
	private String replaceVar(String esIndex, Map<String, Object> paramterMap){
		if(!esIndex.contains("$")) {
			return esIndex;
		}
		for(String key: paramterMap.keySet()) {
			if(paramterMap.get(key)!=null) {
				esIndex = esIndex.replace("${"+key+"}", paramterMap.get(key).toString());
			}
		}
		return esIndex;
	}

}
