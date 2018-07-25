@file:Suppress("MemberVisibilityCanBePrivate")

package example

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TableView
import javafx.util.StringConverter
import tornadofx.*

private val columnNames = listOf("Id", "Name")

private fun emptyRow(): ArrayList<String> {
    return ArrayList<String>().apply {
        repeat(data.first().size) { this.add("") }
    }
}

class RowModel(private val data: ObservableList<ArrayList<String>>, var backRowIndex: Int?) {
    private fun clone(list: ArrayList<String>) = ArrayList(list)
    val row = SimpleObjectProperty(clone(currentRow() ?: emptyRow()))

    private fun currentRow() = backRowIndex?.let { data.elementAt(it) }
    val dirty = SimpleBooleanProperty(false)

    fun valueProperty(index: Int): StringProperty {
        return DataBean(row, index).value
    }

    init {
        this.row.addListener { _, _, newRow ->
            println("currentRow(): ${currentRow()}, new row: $newRow")
            val changed = newRow != currentRow()
            println("changed: $changed")
            dirty.value = changed
        }
    }


    fun rebindOnChange(tableView: TableView<ArrayList<String>>) {
        tableView.selectionModel.selectedIndexProperty().addListener { _, _, index ->
            this.backRowIndex = if (index == -1) null else index.toInt()
            this.row.value = clone(currentRow() ?: emptyRow())
        }
    }

    fun rebindEmpty() {
        this.backRowIndex = null
        this.row.value = emptyRow()
    }

    fun rollback() {
        this.row.value = clone(currentRow() ?: emptyRow())
    }

    fun commit() {
        this.backRowIndex?.let { index ->
            this.data.set(index, clone(this.row.value))
            this.row.value = clone(this.row.value)
        } ?: run {
            this.data.add(clone(this.row.value))
            println("this.data.size: ${this.data.size}")
            this.backRowIndex = this.data.size - 1
            this.row.value = clone(this.data.elementAt(this.backRowIndex!!))
        }
    }

}

private val data = FXCollections.observableArrayList<ArrayList<String>>(
        arrayListOf("111", "AAA"),
        arrayListOf("222", "BBB"),
        arrayListOf("333", "CCC"),
        arrayListOf("444", "DDD")
)

class HelloWorld : View() {

    private val model = RowModel(data, null)

    private lateinit var table: TableView<ArrayList<String>>

    override val root = hbox {
        vbox {
            tableview(data) {
                table = this
                columnNames.forEachIndexed { index, name ->
                    column<ArrayList<String>, String>(name) { it.value[index].toProperty() }.minWidth(100)
                }
                model.rebindOnChange(this)
            }
            hbox {
                button("New Row").setOnAction {
                    model.rebindEmpty()
                }
                button("Delete selected").setOnAction {
                    data.remove(table.selectedItem)
                }
            }
        }
        vbox {
            form {
                fieldset {
                    textProperty.bind(formName())
                    columnNames.forEachIndexed { index, name ->
                        field(name) {
                            textfield(model.valueProperty(index))
                        }
                    }
                    field(forceLabelIndent = true) {
                        button("Rest") {
                            enableWhen(model.dirty)
                            action { model.rollback() }
                        }
                        button("Save") {
                            enableWhen(model.dirty)
                            action {
                                model.commit()
                                table.selectionModel.select(model.backRowIndex!!)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isNewRow(): BooleanBinding {
        return Bindings.createBooleanBinding({ model.backRowIndex == null }, arrayOf(model.row))
    }

    private fun formName(): StringBinding = Bindings.`when`(isNewRow()).then("New").otherwise("Modify")
}

class HelloWorldStyle : Stylesheet() {
    init {
        root {
            prefWidth = 600.px
            prefHeight = 400.px
        }
    }
}

class HelloWorldApp : App(HelloWorld::class, HelloWorldStyle::class)

fun main(args: Array<String>) {
    launch<HelloWorldApp>()
}

//private fun <T, K> ReadOnlyProperty<T>.map(fn: (T) -> K): ReadOnlyProperty<K> {
//    val source = this
//    return ReadOnlyObjectWrapper<K>().apply {
//        source.addListener { _, _, newValue -> this.value = fn(newValue) }
//    }
//}

//private fun <T : String, K> List<ObservableValue<T>>.bindingMap(fn: (List<T>) -> K): ObjectBinding<K> {
//    return Bindings.createObjectBinding({ fn(this.map { it.value }) }, this.toTypedArray())
//}

data class DataBean(val data: SimpleObjectProperty<ArrayList<String>>, val index: Int) {
    val value: SimpleStringProperty = run {
        val p = SimpleStringProperty(this.data.value[index])
        Bindings.bindBidirectional(p, data, object : StringConverter<ArrayList<String>>() {
            override fun toString(obj: ArrayList<String>?): String {
                return obj?.get(index) ?: ""
            }

            override fun fromString(string: String?): ArrayList<String> {
                val newList = ArrayList(data.value)
                newList[index] = string
                return newList
            }
        })
        p
    }
//        get() = run {
//            println("this.data.value: " + this.data.value[index])
//            this.data.
//        }
//        set(value) {
//            val list = ArrayList(this.data.value)
//            list[index] = value
//            this.data.value = list
//        }
}
