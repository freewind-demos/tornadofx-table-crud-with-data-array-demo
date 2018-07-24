@file:Suppress("MemberVisibilityCanBePrivate")

package example

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.TableView
import tornadofx.*

private val columnNames = listOf("Id", "Name")

class RowModel(row: List<Any>) : ViewModel() {
    val backingValue = SimpleObjectProperty(row)
}

private val data = FXCollections.observableArrayList<List<Any>>(
        mutableListOf(111, "AAA"),
        mutableListOf(222, "BBB"),
        mutableListOf(333, "CCC"),
        mutableListOf(444, "DDD")
)

class HelloWorld : View() {

    private val emptyRow = emptyList<Any>()
    private val model = RowModel(emptyRow)

    private lateinit var table: TableView<List<Any>>

    override val root = hbox {
        vbox {
            tableview(data) {
                table = this
                columnNames.forEachIndexed { index, name ->
                    column<List<Any>, String>(name) { it.value[index].toString().toProperty() }.minWidth(100)
                }
                model.rebindOnChange(this) { selectedRow ->
                    backingValue.value = selectedRow ?: emptyRow
                }
            }
            hbox {
                button("New Row").setOnAction {
                    model.rebind { backingValue.value = emptyRow }
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
                            textfield().bind(model.backingValue.map { it.getOrNull(index)?.toString() ?: "" })
                        }
                    }
                    field(forceLabelIndent = true) {
                        button("Rest") {
                            enableWhen(model.dirty)
                            action { model.rollback() }
                        }
                        button("Save") {
                            // enableWhen(model.dirty)
                            action {
                                model.commit()
                                if (isNewRow().value) {
                                    data.add(model.backingValue.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isNewRow(): BooleanBinding {
        return Bindings.createBooleanBinding({ !data.contains(model.backingValue.value) }, arrayOf(model.backingValue))
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

private fun <T, K> ReadOnlyProperty<T>.map(fn: (T) -> K): ReadOnlyProperty<K> {
    val source = this
    return ReadOnlyObjectWrapper<K>().apply {
        source.addListener { _, _, newValue -> this.value = fn(newValue) }
    }
}

private fun <T : Any, K> List<ObservableValue<T>>.bindingMap(fn: (List<T>) -> K): ObjectBinding<K> {
    return Bindings.createObjectBinding({ fn(this.map { it.value }) }, this.toTypedArray())
}