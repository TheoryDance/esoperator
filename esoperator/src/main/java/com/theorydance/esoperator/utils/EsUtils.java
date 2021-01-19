package com.theorydance.esoperator.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 默认使用的es地址"http://127.0.0.1:9200/"，如果需要设置其他地址，可以通过setEsHost进行修改
 */
public class EsUtils {

	/**
	 * es连接地址，默认值为"http://127.0.0.1:9200/"
	 */
    private static String esHost = "http://127.0.0.1:9200/";

    /**
     * @param index  索引
     * @param dsl    kibana中的命令，类似SQL，里面可以含有${}， #{}参数，示例<br>
     *               {<br>
     *               &emsp;"query": {<br>
     *               &emsp;&emsp;"match": {<br>
     *               &emsp;&emsp;&emsp;"project": #{modelSMS}<br>
     *               &emsp;&emsp;}<br>
     *               &emsp;},<br>
     *               &emsp;"sort": [<br>
     *               &emsp;&emsp;{<br>
     *               &emsp;&emsp;&emsp;"@timestamp": {<br>
     *               &emsp;&emsp;&emsp;&emsp;"order": "desc"<br>
     *               &emsp;&emsp;&emsp;}<br>
     *               &emsp;&emsp;}<br>
     *               &emsp;],<br>
     *               &emsp;"size": #{size}<br>
     *               }
     * @param params 设置dsl中的参数值
     * @return
     */
    public static JSONArray getEsData(String index, String dsl, Map<String, Object> params,Map<String,Object> connMap) {
        try {
            dsl = getCompleteDsl(dsl, params);
            String body = send(esHost + index + "/_search", dsl,connMap);
            JSONObject jsonbody = JSONObject.fromObject(body);
            JSONArray hits = jsonbody.getJSONObject("hits").getJSONArray("hits");
            JSONArray resultArray = new JSONArray();
            for (Object object : hits) {
                JSONObject item = (JSONObject) object;
                JSONObject source = item.getJSONObject("_source");
                resultArray.add(source);
            }
            return resultArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param index  索引
     * @param dsl    kibana中的命令，类似SQL，里面可以含有${}， #{}参数，示例<br>
     *               {<br>
     *               &emsp;"query": {<br>
     *               &emsp;&emsp;"match": {<br>
     *               &emsp;&emsp;&emsp;"project": #{modelSMS}<br>
     *               &emsp;&emsp;}<br>
     *               &emsp;},<br>
     *               &emsp;"sort": [<br>
     *               &emsp;&emsp;{<br>
     *               &emsp;&emsp;&emsp;"@timestamp": {<br>
     *               &emsp;&emsp;&emsp;&emsp;"order": "desc"<br>
     *               &emsp;&emsp;&emsp;}<br>
     *               &emsp;&emsp;}<br>
     *               &emsp;],<br>
     *               &emsp;"size": #{size}<br>
     *               }
     * @param params 设置dsl中的参数值
     * @return
     */
    public static List<Map<String, Object>> getEsAggsData(String index, String dsl, String bucketsName, Map<String, Object> params,Map<String,Object> connMap) {
        try {
            dsl = getCompleteDsl(dsl, params);
            String body = send(esHost + index + "/_search", dsl,connMap);
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(body);
            List<Map<String, Object>> list = new ArrayList<>();
            getJSONData(jsonObject,bucketsName,list);
            return  list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param json 要解析的数据对象
     * @param attrExpression aggregations.carno_maxgpstime.buckets[].gps_time_max.hits.hits[0]._source->[gps_time,car_no,latitude,longitude]
     *             [] 代表数组，有数字代表取特定下标的值，没数据代表数组中的数据全取
     *             ->[]代表最终要取的数据字段，用,分隔
     * @param list  存储解析后的数据
     * @return
     */
    public static void getJSONData(com.alibaba.fastjson.JSONObject json, String attrExpression, final List<Map<String, Object>> list) {
        String attr = null; // 表达式中的第一个属性
        String nextAttrExpression = null;
        if(attrExpression.split("->")[0].contains(".")) {
        	attr = attrExpression.substring(0, attrExpression.indexOf("."));
        	nextAttrExpression = attrExpression.substring(attrExpression.indexOf(".") + 1);
        }else {
        	attr = attrExpression.substring(0, attrExpression.indexOf("->"));
        	nextAttrExpression = attrExpression.substring(attrExpression.indexOf("->"));
        }

    	if(attr.equals("")) {
    		// 去掉最前面的 "->[" 和 最后面的 "]"
    		String tempStr = nextAttrExpression.replace("->[", "");
    		String[] sourceAttr = tempStr.substring(0, tempStr.length()-1).split(",");

    		Map<String,Object> map = new HashMap<>();
    		if(sourceAttr.length==0||(sourceAttr.length==1&&sourceAttr[0].equals("*"))) {
    			Set<String> keys = json.keySet();
    			for (String key : keys) {
    				map.put(key, json.get(key));
				}
    		}else {
    			for (final String x : sourceAttr) {
        			String key = x;
        			Object value = null;
        			if(x.contains(" as ")) {
        				key = x.split("\\sas\\s")[1].trim();
        			}else {
        				key= x.trim();
        			}

        			if(x.contains(" as ")) {
        				String t = x.split("\\sas\\s")[0].trim(); // 此处的t可能是多级，也可能是数据的具体下表，也可能是一个对象
        				value = getAttrValue(json, t);
    					if(value instanceof Date) {
    						value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date)value);
    					}
    				}else {
    					value = getAttrValue(json, x.trim());
    				}
        			if(JSONObject.fromObject(null).equals(value)) {
        				value = null;
        			}
    				map.put(key, value);
    			}
    		}
        	list.add(map);
    	}else if(isArray(attr)) {
    		String attrName = attr.split("\\[")[0];
    		Integer indexNum = getIndexNum(attr);
    		if(indexNum == null) { // 为[],进行遍历
                com.alibaba.fastjson.JSONArray array = json.getJSONArray(attrName);
    			for (Object object : array) {
    				getJSONData(( com.alibaba.fastjson.JSONObject)object, nextAttrExpression, list);
				}
    		}else { // [Num]
                com.alibaba.fastjson.JSONObject son = json.getJSONArray(attrName).getJSONObject(indexNum);
    			getJSONData(son, nextAttrExpression, list);
    		}
    	}else {
            com.alibaba.fastjson.JSONObject son = json.getJSONObject(attr);
    		getJSONData(son, nextAttrExpression, list);
    	}
    }

    /**
     * 多级方式获取结果，取出来的是一个对象
     * @param json 不能为null
     * @param key 可能是多级方式，也可能含有[下标]方式
     * @return
     */
    private static Object getAttrValue( com.alibaba.fastjson.JSONObject json, final String key) {
		if(!key.contains(".")&&!key.contains("[")) {
			return json.get(key);
		}
		List<Map<String, Object>> list = new ArrayList<>();
		if(!key.endsWith("]")) {
			int i = key.lastIndexOf(".");
			String resultName = key.substring(i+1);
			String index = key.substring(0, i);
			getJSONData(json, index+"->["+resultName+"]", list);
			if(list.size()>0) {
				return list.get(0).get(resultName);
			}
		}else {
			if(key.contains(".")) {
				int i = key.lastIndexOf(".");
				String resultName = key.substring(i+1);
				Integer indexNum = getIndexNum(resultName);
				if(indexNum!=null) {
					resultName = resultName.replace("["+indexNum+"]", "");
					String index = key.substring(0, i);
					getJSONData(json, index+"->["+resultName+"]", list);
					if(list.size()>0) {
						Object obj = list.get(0).get(resultName);
						return JSONArray.fromObject(obj).get(indexNum);
					}
				}else {
					resultName = resultName.replace("[]", "");
					String index = key.substring(0, i);
					getJSONData(json, index+"->["+resultName+"]", list);
					if(list.size()>0) {
						return list.get(0).get(resultName);
					}
				}
			}else {
				Integer indexNum = getIndexNum(key);
				if(indexNum!=null) {
					String resultName = key.replace("["+indexNum+"]", "");
					getJSONData(json, "->["+resultName+"]", list);
					if(list.size()>0) {
						Object obj = list.get(0).get(resultName);
						return JSONArray.fromObject(obj).get(indexNum);
					}
				}else {
					String resultName = key.replace("[]", "");
					getJSONData(json, "->["+resultName+"]", list);
					if(list.size()>0) {
						return list.get(0).get(resultName);
					}
				}

			}
		}
		return null;
	}

	/**
     * 判断表达式是否时数组表达式
     */
    public static boolean isArray(String expression) {
    	if(expression.contains("[")) {
    		return true;
    	}
    	return false;
    }

    public static Integer getIndexNum(String arrayExpression) {
        Pattern p = Pattern.compile(".*\\[([0-9]+)\\]");
        Matcher m = p.matcher(arrayExpression);
        boolean b = m.find();
        if(b){
        	return Integer.parseInt(m.group(1));
        }
        return null;
    }

    /**
     * 这里使用原始的http请求连接，没有使用http相关的工具包（比如hutool），是因为在基础模块中可能没有相关依赖，直接使用java自带的
     */
    public static String send(String url, String body, Map<String, Object> connMap) {
    	if(!url.startsWith("http://") && !url.startsWith("https://")) {
    		url = esHost + url;
    	}
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream input = null;
        ByteArrayOutputStream outputs = null;
        int connectTimeout = 1000;
        int readTimeout = 3000;
        if (connMap != null) {
            connectTimeout = objToint(connMap.get("connectTimeout")) > 0 ? objToint(connMap.get("connectTimeout")) : 1000;
            readTimeout = objToint(connMap.get("readTimeout")) > 0 ? objToint(connMap.get("readTimeout")) : 3000;
        }
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.addRequestProperty("Content-Type", "application/json");
            out = conn.getOutputStream();
            out.write(body.getBytes());
            out.flush();
            input = conn.getInputStream();
            
            int size = 1024*5;
            int len=0;
            outputs = new ByteArrayOutputStream();
            StringBuffer content = new StringBuffer(size);
            byte[] buf = new byte[size];
            while((len=input.read(buf))!=-1) {
                outputs.write(buf, 0, len);
            }
            
            content.append(new String(outputs.toByteArray()));
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(out);
        close(input);
        close(outputs);
        return null;
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
            }
        }
    }

    private static String getCompleteDsl(String dsl, Map<String, Object> params) throws Exception {
        Set<String> keys = findParams(dsl);
        for (String key : keys) {
            String mapKey = key.replace("$", "").replace("#", "").replace("{", "").replace("}", "");
            Object value = params.get(mapKey);
            if (value == null) {
                throw new Exception("没有对应的参数，请设置");
            }
            if (key.startsWith("$")) {
                dsl = dsl.replace(key, value.toString());
            } else {
                // 如果有其他类型，自己在这里继续扩展
                if (value instanceof Integer ||
                        value instanceof Float ||
                        value instanceof Double) {
                    dsl = dsl.replace(key, value.toString());
                } else { // 非数字就以字符串处理
                    dsl = dsl.replace(key, "\"" + value.toString() + "\"");
                }
            }
        }
        return dsl;
    }

    private static Set<String> findParams(String dsl) {
        Set<String> params = new HashSet<>();
        Pattern p = Pattern.compile("([#\\$]\\{.*?\\})");
        Matcher m = p.matcher(dsl);
        boolean b = m.find();
        while (b) {
            String value = m.group(1);
            params.add(value);
            b = m.find();
        }
        return params;
    }

    public static void setEsHost(String esHost) {
        EsUtils.esHost = esHost;
    }
    public static int objToint(Object obj) {
        return (obj != null && !"".equals(obj)) ? Integer.parseInt(obj.toString()) : 0;
    }
}
