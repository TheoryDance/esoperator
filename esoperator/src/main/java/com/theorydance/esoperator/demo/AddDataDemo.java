package com.theorydance.esoperator.demo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.theorydance.esoperator.utils.EsUtils;

import net.sf.json.JSONObject;

public class AddDataDemo {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static void main(String[] args) throws Exception{
		
		Calendar c = Calendar.getInstance();
		c.setTime(sdf.parse("2021-01-08 13:00:00"));
		Calendar e = Calendar.getInstance();
		e.setTime(sdf.parse("2021-01-08 14:00:00")); // "2020-12-29 14:00:00"
		while(c.getTimeInMillis() <= e.getTimeInMillis()) {
			addDataToEs(sdf.format(c.getTime()));
			c.add(Calendar.HOUR_OF_DAY, 1);
		}
		
	}
	
	private static void addDataToEs(String time) {
		try {
			List<List<String>> cityList = getNeedCitys();
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(time));
			String stime = time;
			for(int i=0;i<120;i++) {
				c.add(Calendar.HOUR_OF_DAY, 1);
				String etime = sdf.format(c.getTime());
				for (List<String> list : cityList) {
					send(list.get(0), list.get(1), list.get(2), list.get(3), stime, etime);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static List<List<String>> getNeedCitys() {
		List<List<String>> cityList = new ArrayList<>();
		cityList.add(Arrays.asList("北京市", "110000", "北京市", "110000"));
		cityList.add(Arrays.asList("成都市", "510100", "四川省", "510000"));
		return cityList;
	}
	
	
	public static void send(String city, String city_code, String province, String province_code, String stime, String etime) {
		System.out.println("向es中forecast_city_hour_air上传指定日期的数据：" + stime+"，城市：" + city);
		JSONObject body = new JSONObject();
		body.put("stime", stime);
		body.put("etime", etime);
		body.put("aqi", getInt(500));
		body.put("pm10", getInt(1000));
		body.put("pm25", getInt(350));
		body.put("so2", getInt(300));
		body.put("no2", getInt(300));
		body.put("co", getDouble());
		body.put("o3", getInt(160));
		body.put("o38", getInt(800));
		body.put("quality", ""+(1+getInt(5))); // 1,2,3,4,5,6
		body.put("city", city);
		body.put("city_code", city_code);
		body.put("province", province);
		body.put("province_code", province_code);
		body.put("primary_pollutant", getPrimaryPollu());
		body.put("remark", "测试");
		String response = EsUtils.send("forecast_city_hour_air/info", body.toString(), null);
		System.out.println(response);
	}
	private static String getPrimaryPollu() {
		String[] parimaryArr = new String[] {"PM10","PM2.5","O3"};
		return parimaryArr[(int)(Math.random()*2)];
	}
	
	public static int getInt(int base) {
		return (int)(Math.random() * base);
	}
	public static double getDouble() {
		return Math.random() * 0.5 + 0.8;
	}
	

}
