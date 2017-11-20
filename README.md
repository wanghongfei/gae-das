# GAE-DAS
监听mysql binlog并生成GAE使用的增量索引, 输出至本地文件.

项目还处于开发状态

## 模板
通过配置模板`template.json`来指定索引如何生成:
```
{
    "database": "gae-das", # 库名
    "tableList": [
        {
            "tableName": "new_table", # 表名
            "level": 1, # 该表所在层级

			# 对 insert 操作感兴趣
            "insert": [
            	# 输出索引记录的字段顺序
            	# 此例为
            	# id\tname\tage
                {"column": "id"},
                {"column": "name"},
                {"column": "age"}
            ],
            "update": [
                {"column": "id"},
                {"column": "name"},
                {"column": "age"}
            ],
            "delete": [
                {"column": "id"},
                {"column": "name"},
                {"column": "age"}
            ]
        },
        {
            "tableName": "acc",
            "level": 2,

            "insert": [
                {"column": "id"},
                {"column": "name"}
            ],
            "update": [
                {"column": "id"},
                {"column": "name"}
            ],
            "delete": [
                {"column": "id"},
                {"column": "name"}
            ]
        }

    ]
}
```
输出的索引文件为词表(`\t`分隔):
```
# 层级	# 操作类型(0:insert, 1:update, 2: delete)		# 数据项(模板中的column)
1	0	6	apple
1	1	6	apples
2	2	5	aaa
2	0	7	pear
```
mysql连接信息和binlog同步设置见`application.yml`文件

## 构建运行
```
mvn clean package -Dmaven.test.skip=true
java -jar gae-das.jar
```
然后修改配置文件中的mysql连接信息和`template.json`模板即可
