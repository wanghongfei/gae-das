# GAE-DAS
监听mysql binlog并生成GAE使用的增量索引, 保存至本地文件

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
