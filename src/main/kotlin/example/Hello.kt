@file:Suppress("MemberVisibilityCanBePrivate")

package example

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.control.TableView
import tornadofx.*

class User(id: Int = 0, name: String? = null) {
    val id = SimpleIntegerProperty(id)
    val name = SimpleStringProperty(name)
}

class UserModel(user: User) : ViewModel() {
    val backingValue = SimpleObjectProperty(user)
    val id = bind { backingValue.value.id }
    val name = bind { backingValue.value.name }
}

private val data = FXCollections.observableArrayList<User>(User(111, "AAA"), User(222, "BBB"), User(333, "CCC"), User(444, "DDD"))

class HelloWorld : View() {

    private val model = UserModel(User())

    private lateinit var table: TableView<User>

    override val root = hbox {
        vbox {
            tableview(data) {
                table = this
                column("id", User::id).minWidth(80)
                column("name", User::name).minWidth(200)
                model.rebindOnChange(this) { selectedUser ->
                    backingValue.value = selectedUser ?: User()
                }
            }
            hbox {
                button("New User").setOnAction {
                    model.rebind { backingValue.value = User() }
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
                    field("Id") {
                        textfield(model.id)
                    }
                    field("Name") {
                        textfield(model.name)
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
                                if (isNewUser().value) {
                                    data.add(model.backingValue.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isNewUser(): BooleanBinding {
        return Bindings.createBooleanBinding({ !data.contains(model.backingValue.value) }, arrayOf(model.backingValue))
    }

    private fun formName(): StringBinding = Bindings.`when`(isNewUser()).then("New").otherwise("Modify")
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