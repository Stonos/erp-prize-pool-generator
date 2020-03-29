@file:Suppress(
    "INTERFACE_WITH_SUPERCLASS",
    "OVERRIDING_FINAL_MEMBER",
    "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
    "CONFLICTING_OVERLOADS",
    "EXTERNAL_DELEGATION"
)

external interface MagicGridProps {
    var container: dynamic /* String | HTMLElement */
        get() = definedExternally
        set(value) = definedExternally
    var static: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var items: Number?
        get() = definedExternally
        set(value) = definedExternally
    var gutter: Number?
        get() = definedExternally
        set(value) = definedExternally
    var maxColumns: Number?
        get() = definedExternally
        set(value) = definedExternally
    var useMin: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var useTransform: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var animate: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

@JsModule("magic-grid")
@JsNonModule
external open class MagicGrid(config: MagicGridProps) {
    open fun listen()
    open fun positionItems()
    open fun ready(): Boolean
    open fun init()
    open fun colWidth(): Number
    open fun setup(): Any?
    open fun nextCol(cols: Array<Any?>, i: Number): Any?
    open fun getReady()
}

external fun checkParams(config: MagicGridProps)

external fun getMin(cols: Array<Any?>): Any?