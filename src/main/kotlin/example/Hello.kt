@file:Suppress("MemberVisibilityCanBePrivate")

package example

import com.github.freewind.lostlist.Lists
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableView
import javafx.util.StringConverter
import tornadofx.*

private val columnNames = listOf("Id", "Name")

private val data = FXCollections.observableArrayList<RowBean>(
        RowBean(arrayListOf("111", "AAA")),
        RowBean(arrayListOf("222", "BBB")),
        RowBean(arrayListOf("333", "CCC")),
        RowBean(arrayListOf("444", "DDD"))
)

class RowBean(row: ArrayList<String> = Lists.arrayListFilled(columnNames.size, "")) {
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

class RowModel(row: RowBean) : ViewModel() {
    val rowProperty = SimpleObjectProperty(row)
    fun cellProperty(index: Int) = bind { rowProperty.value.cellProperty(index) }
}


class HelloWorld : View() {

    private val model = RowModel(RowBean())

    private lateinit var table: TableView<RowBean>

    override val root = hbox {
        vbox {
            tableview(data) {
                table = this
                columnNames.forEachIndexed { index, name ->
                    column<RowBean, String>(name) { it.value.cellProperty(index) }.minWidth(100)
                }
                model.rebindOnChange(this) { selectedRow ->
                    rowProperty.value = selectedRow
                }
            }
            hbox {
                button("New Row").setOnAction {
                    model.rebind {
                        model.rowProperty.value = RowBean()
                    }
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
                            textfield(model.cellProperty(index))
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
                                if (isNewRow().value) {
                                    data.add(model.rowProperty.value)
                                    table.selectionModel.selectLast()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isNewRow(): BooleanBinding {
        return Bindings.createBooleanBinding({ !data.contains(model.rowProperty.value) }, arrayOf(model.rowProperty))
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

