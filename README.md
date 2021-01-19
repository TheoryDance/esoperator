# esoperator
一个通过http请求对elasticsearch进行数据查询或操作的项目

## 描述：
在之前的项目中，一直使用mybatis，习惯了使用sql语句进行查询和处理数据，当切换到elasticsearch数据源后，发现需要通过其对应的高级编程接口对数据进行操作和查询，而且返回结果一层一层的，光解析数据，都挺麻烦，故而参考mybatis方式，自己针对http请求进行了一个封装，在kibana中编写查询语句，然后复制作为查询语言（类似mysql中的sql语句），然后对查询结果进行解析定义，框架进行自动解析。

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
