package cn.sexycode.util.core.cls;

/**
 * Represent an invokable method
 * <p/>
 * The underlying layer does not guaranty that xProperty == xMethod
 * if the underlying artefact is the same
 * However xProperty.equals(xMethod) is supposed to return true
 */
public interface XMethod extends XMember {
}
