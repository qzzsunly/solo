package cn.sexycode.util.core.cls;

/**
 * A member which actually is a property (as per the JavaBean spec)
 * Note that the same underlying artefact can be represented as both
 * XProperty and XMethod
 * The underlying layer does not guaranty that xProperty == xMethod
 * if the underlying artefact is the same
 * However xProperty.equals(xMethod) is supposed to return true
 */
public interface XProperty extends XMember {
}
