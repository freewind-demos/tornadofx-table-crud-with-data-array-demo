TornadoFX Table Dynamic CRUD with Data Array Demo
=================================================

Display data in a table and provide CRUD demo. The table is considered dynamic, the column names and data can be retrieved from outside, e.g. from database.

Currently, there are several issues:

1. The dirty checking always returns `false`, so the `reset` button is always disabled
2. If we enable the `reset` button, it can't reset the form data to original
3. When click on the `save` button, it can't update data in the left table.

Run `Hello.kt` file in your IDE.
