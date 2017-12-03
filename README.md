# GAE-DAS
监听mysql binlog并生成GAE使用的增量索引, 输出至本地文件或kafka. 

GAE-DAS使用[mysql-binlog-connector](https://github.com/shyiko/mysql-binlog-connector-java)库进行binlog监听。程序启动连接成功后会查询`information_schema`库的`columns`表来指定数据库所有表的元数据(列名，位置，数据类型等)，并使用该数据解析`TABLE_MAP`事件。因此，当数据表结构发生变化时，最好重启一下DAS。(除非新增列、位于所有列的最后且该列不涉及索引生成)



项目还处于开发状态。

## 模板
通过配置模板`template.json`来指定索引如何生成(配置对哪些表中的哪些字段感兴趣):
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
增量索引写到文件时, 除mysql连接信息和`template.json`外不需要额外配置:
```
mvn clean package -Dmaven.test.skip=true
java -jar gae-das.jar
```
增量索引写入kafka时, 需要配置kafka相关信息(见`application.yml`中`das.store.kafka`相关配置)
