# esoperator
一个通过http请求对elasticsearch进行数据查询或操作的项目

## 描述：
在之前的项目中，一直使用mybatis，习惯了使用sql语句进行查询和处理数据，当切换到elasticsearch数据源后，发现需要通过其对应的高级编程接口对数据进行操作和查询，而且返回结果一层一层的，光解析数据，都挺麻烦，故而参考mybatis方式，自己针对http请求进行了一个封装，在kibana中编写查询语句，然后复制作为查询语言（类似mysql中的sql语句），然后对查询结果进行解析定义，框架进行自动解析。

## demo
在目录src/main/java/com/theorydance/esoperator/demo/ 下面，里面有一个新增数据操作和查询数据操作

## 示例：
```
public interface TestMapper {
	List<Map<String, Object>> getDataList(@Esparam("remark") String remark, @Esparam("size") int pagesize);
}
```

```
<?xml version="1.0" encoding="UTF-8" ?>
<elasticsearch>
   	<dsl id="getDataList" esIndex="forecast_city_hour_air" parseResult="hits.hits[]._source->[aqi,pm10,pm25,so2,no2,co,o3,o38,quality,city,city_code,province,province_code]">
{
  "query": {
    "bool": {
      "must": [
        {
          "term": {
            "remark.keyword": {
              "value": #{remark}
            }
          }
        }
      ]
    }
  },
  "size": #{size}
}
	</dsl>
</elasticsearch>
```

就可以直接将es中的数据取出到List<Map<String,Object>>中了

## 测试数据
```
{
  "took" : 60,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 419162,
    "max_score" : 4.908421E-6,
    "hits" : [
      {
        "_index" : "forecast_city_hour_air",
        "_type" : "info",
        "_id" : "_lfEmHYBdAMslb98Zb4c",
        "_score" : 4.908421E-6,
        "_source" : {
          "stime" : "2020-12-01 00:00:00",
          "etime" : "2020-12-01 12:00:00",
          "aqi" : 397,
          "pm10" : 215,
          "pm25" : 13,
          "so2" : 227,
          "no2" : 24,
          "co" : 1.0916091255026077,
          "o3" : 92,
          "o38" : 446,
          "quality" : "1",
          "city" : "广元市",
          "city_code" : "510800",
          "province" : "四川省",
          "province_code" : "510000",
          "primary_pollutant" : "PM10",
          "remark" : "测试"
        }
      },...
    ]
  }
}
```

## 使用说明
1、xml文件中的<dsl>标签中的esIndex就是es中的索引，parseResult属性是控制如何对查询的结果进行解析，解析的具体代码实现是在com.theorydance.esoperator.utils.EsUtils中实现的；<br>

2、parseResult属性中可以对需要返回的属性进行重命名，比如
```
hits.hits[]._source->[aqi,pm10,pm25 as pm2_5,so2,no2,co,o3,o38,quality,city,city_code,province,province_code]
```
返回结果
```
{o3=92, city=广元市, pm10=215, city_code=510800, co=1.0916091255026077, province_code=510000, quality=1, no2=24, pm25=13, province=四川省, o38=446, so2=227, aqi=397}
{o3=33, city=南充市, pm10=243, city_code=511300, co=1.0839999451269338, province_code=510000, quality=4, no2=43, pm25=175, province=四川省, o38=280, so2=79, aqi=377}
{o3=134, city=达州市, pm10=570, city_code=511700, co=0.8754323620008577, province_code=510000, quality=1, no2=53, pm25=162, province=四川省, o38=679, so2=282, aqi=301}
{o3=78, city=巴中市, pm10=912, city_code=511900, co=1.0770642456450192, province_code=510000, quality=2, no2=26, pm25=61, province=四川省, o38=385, so2=92, aqi=92}
...
```
上面的这种返回的内容都在同一层次中；<br>

3、针对返回的结果不在统一层次中，同样可以在parseResult中进行指定，让其自动解析，比如：
```
parseResult="hits.hits[]->[_score,_source.aqi as aqi,_source.pm10 as pm10]"
```
返回结果
```
{aqi=397, pm10=215, _score=0.000004908421}
{aqi=377, pm10=243, _score=0.000004908421}
{aqi=301, pm10=570, _score=0.000004908421}
{aqi=92, pm10=912, _score=0.000004908421}
{aqi=42, pm10=858, _score=0.000004908421}
...
```

4、在xml中使用的#{}符号，这个是参考mybatis方式，当使用#{}的时候，会根据传递的参数是数值还是字符串，自动判断是否需要添加双引号"",如果是使用${}符号的话，就是纯粹的字符串替换，<dsl>中可以在非id属性上使用${}，来达到动态指定索引和解析返回结果，也可以在dsl中查询语句中使用#{}和${}来动态的传递参数，分页等。<br>

5、在parseResult属性的值中，除了可以指定解析[]下所有对象，也是可以在里面指定具体数值，解析某一个对象的，比如
```
parseResult="hits.hits[0]->[_score,_source.aqi as aqi,_source.pm10 as pm10]"
```

6、注意，mapper.class接口和mapper.xml文件需要在同一个目录中，在springboot中使用的时候，需要在pom.xml中添加代码：
```
<build>
  <resources>
	<resource>
	  <directory>src/main/resources</directory>
	  <includes>
		<include>**/**</include>
	  </includes>
	  <filtering>false</filtering>
	</resource>
	<resource>
	  <directory>src/main/java</directory>
	  <includes>
		<include>**/*.properties</include>
		<include>**/*.xml</include>
		<include>**/*.tld</include>
	  </includes>
	  <filtering>false</filtering>
	</resource>
  </resources>
</build>
```
