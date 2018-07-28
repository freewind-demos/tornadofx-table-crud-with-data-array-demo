TornadoFX Table Dynamic CRUD with Data Array Demo
=================================================

Display data in a table and provide CRUD demo. The table is considered dynamic, the column names and data can be retrieved from outside, e.g. from database.

Currently, there are several issues:

1. The dirty checking always returns `false`, so the `reset` button is always disabled
2. If we enable the `reset` button, it can't reset the form data to original
3. When click on the `save` button, it can't update data in the left table.

Run `Hello.kt` file in your IDE.

A question for this demo: <https://stackoverflow.com/questions/51503733/how-to-modify-rows-in-a-tableview-which-has-data-array-as-rows>

Update
------

Fixed the code by creating a `RowBean` to wrap the array list as a JavaBean, also provided a property for each cell:

```
class RowBean(row: ArrayList<String>) {
    val rowProperty = SimpleObjectProperty(row)
    fun cellProperty(index: Int): SimpleStringProperty = SimpleStringProperty(this.rowProperty.value[index]).apply {
        Bindings.bindBidirectional(this, rowProperty, object : StringConverter<ArrayList<String>>() {
            override fun toString(obj: ArrayList<String>?): String {
                return obj?.get(index) ?: ""
            }

            override fun fromString(string: String?): ArrayList<String> {
                val newList = Lists.copy(rowProperty.value)
                newList[index] = string
                return newList
            }
        })
    }
}
```

Then use it as a normal JavaBean with the `ViewModel` as usual.