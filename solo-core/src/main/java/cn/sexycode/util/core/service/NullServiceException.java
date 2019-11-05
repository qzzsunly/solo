package cn.sexycode.util.core.service;

/**
 *
 */
public class NullServiceException extends RuntimeException {
    public final Class serviceRole;

    public NullServiceException(Class serviceRole) {
        super("Unknown service requested [" + serviceRole.getName() + "]");
        this.serviceRole = serviceRole;
    }

    public Class getServiceRole() {
        return serviceRole;
    }
}
