/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package cn.sexycode.util.core.cls;

import java.lang.annotation.Annotation;

/**
 *
 */
public interface XAnnotatedElement {

    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType);

    Annotation[] getAnnotations();

    /**
     * Returns true if the underlying artefact
     * is the same
     */
    boolean equals(Object x);
}
