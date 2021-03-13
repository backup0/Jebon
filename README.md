# Jebon

#### NOTE: the module name is _snarkyJebon_.

### Reading json

To retrieve a value from a json field call `getItem(..)`. The _key_ is the full "path" to the item, that is the name of the field preceded by the names of all the nested objects. For example `getItem("email", "work")` means get the value for the field _"work"_ which is inside the object _"email"_, which is inside the root object. Suppose we have the following json
```
{
"email" :  {
  "work" : "email@domain", "personal" : "email2@domain", ..
  }, ..
}
```
the aforementioned method call will return _"email@domain"_.  Values inside arrays can be retrieved in the same way, the last item in the path is the index. For example `getItem("email", "1")` means get the value inside an array _"email"_ at index _"1"_. Suppose we have the following json
```
{
"email" :  ["email1@domain", "email2@domain", ....], ..
}
```
the method call will return _"email2@domain"_.

`getItem(..)` returns a `JSONItem` instance. The actual value returned depends on the data type of the field.

- JSONTypes.STRING: String.
- JSONTypes.OBJECT: String array containing the names of all fields inside the object.
- JSONTypes.ARRAY: String array; like JSONTypes.OBJECT but the names are integers representing the indice.
- JSONTypes.BOOLEAN: Boolean.
- JSONTypes.NUMBER: Double.
- JSONTypes.NULL: null.

### Creating json

To insert fields (they key-value pair), call of one the `put` methods. The key is the full path to the field to be created. 
Array can be created on the fly by providing an integer as the last item in the key. For example `put(true, "array", "1")` will create an array called _"array"_, and the value _"true"_ will be placed at index _"1"_. More items can be added to the array by putting them at different indice; for example `put(false, "array", "2")` will put the value _"false"_ at index _"2"_, etc. Any figure can be used for the index as long as it is an integer, it doesn't have to be incremental, doesn't have to start at 0, doesn't have to be sequential. To insert empty array or object use `put(SpecialType val, String... keys)` method.

Example
```
JSONCreator jk = new JSONCreator(true);
// create a nested object "obj" with one field "val"
jk.put(31, "obj", "nested-obj", "val");
// create an array "array" inside "obj", put the value "false"
// at the index 1.
jk.put(false,"obj", "array", "1");
// put the value "false" in the same array @ index 2
jk.put(true,"obj", "array", "2");
// put the value "false" in the same array @ index 2
jk.put(100,"obj", "array", "1024");
// put empty array inside the root object
jk.put(SpecialType.JSONArray, "array-2");
// put objects inside the new array.
jk.put(SpecialType.JSONNull, "array-2", "1", "in-obj-at-1");
jk.put(SpecialType.JSONNull, "array-2", "2", "in-obj-at-2");
```

Json created, 
```
{
  "obj": {
    "array": [
      false,
      true,
      100
    ],
    "nested-obj": {
      "val": 31
    }
  },
  "array-2": [
    {
      "in-obj-at-1": null
    },
    {
      "in-obj-at-2": null
    }
  ]
}
```
