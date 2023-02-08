package cz.zcu.jsmahy.datamining.api;

import javafx.concurrent.Task;

/**
 * The task <b>MUST</b> be stateless. That means, each call to the method {@link Task#call()} <b>MUST NOT</b> modify the object's state. This is required as only one instance of this task is created
 * and can be reused.
 */
abstract class DataNodeExportTask<V> extends Task<V> {}
