>   标识符说明：
>
>   type：对象的类型
>
>   prefix：当对象为item类型的子类型时，prefix为item:type，比如书籍的prefix为item:book
>
>   ​			 其他情况prefix都为对应的类型，比如怪物的prefix为monster
>
>   ident：对象在对应类型下的唯一标识符，详见[JSON_INFO.md](https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INFO.md)
>
>   name：对象name字段经过翻译后的值

-   prefix:ident ->  json obj

    根据prefix和ident查询得到json

    e.g. `monster:mon_dog_thing -> {...} (一个json)`

-   prefix:name  -> prefix:ident

    根据prefix和name查询得到上述索引key

    e.g. `monster:狗 -> monster:mon_dog_thing`