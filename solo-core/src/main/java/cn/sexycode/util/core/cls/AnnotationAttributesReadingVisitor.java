package cn.sexycode.util.core.cls;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.sexycode.util.core.collection.MultiValueMap;
import cn.sexycode.util.core.object.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ASM visitor which looks for the annotations defined on a class or method, including
 * tracking meta-annotations.
 *
 * <p>As of Spring 3.1.1, this visitor is fully recursive, taking into account any nested
 * annotations or nested annotation arrays. These annotations are in turn read into
 * {@link AnnotationAttributes} map structures.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.0
 */
final class AnnotationAttributesReadingVisitor extends RecursiveAnnotationAttributesVisitor {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationAttributesReadingVisitor.class);
	private final MultiValueMap<String, AnnotationAttributes> attributesMap;

	private final Map<String, Set<String>> metaAnnotationMap;


	public AnnotationAttributesReadingVisitor(String annotationType,
			MultiValueMap<String, AnnotationAttributes> attributesMap, Map<String, Set<String>> metaAnnotationMap,
			ClassLoader classLoader) {

		super(annotationType, new AnnotationAttributes(annotationType, classLoader), classLoader);
		this.attributesMap = attributesMap;
		this.metaAnnotationMap = metaAnnotationMap;
	}


	@Override
	public void visitEnd() {
		super.visitEnd();

		Class<?> annotationClass = this.attributes.annotationType();
		if (annotationClass != null) {
			List<AnnotationAttributes> attributeList = this.attributesMap.get(this.annotationType);
			if (attributeList == null) {
				this.attributesMap.add(this.annotationType, this.attributes);
			}
			else {
				attributeList.add(0, this.attributes);
			}
			Set<Annotation> visited = new LinkedHashSet<Annotation>();
			Annotation[] metaAnnotations = AnnotationUtils.getAnnotations(annotationClass);
			if (!ObjectUtils.isEmpty(metaAnnotations)) {
				for (Annotation metaAnnotation : metaAnnotations) {
					if (!AnnotationUtils.isInJavaLangAnnotationPackage(metaAnnotation)) {
						recursivelyCollectMetaAnnotations(visited, metaAnnotation);
					}
				}
			}
			if (this.metaAnnotationMap != null) {
				Set<String> metaAnnotationTypeNames = new LinkedHashSet<String>(visited.size());
				for (Annotation ann : visited) {
					metaAnnotationTypeNames.add(ann.annotationType().getName());
				}
				this.metaAnnotationMap.put(annotationClass.getName(), metaAnnotationTypeNames);
			}
		}
	}

	private void recursivelyCollectMetaAnnotations(Set<Annotation> visited, Annotation annotation) {
		Class<? extends Annotation> annotationType = annotation.annotationType();
		String annotationName = annotationType.getName();
		if (!AnnotationUtils.isInJavaLangAnnotationPackage(annotationName) && visited.add(annotation)) {
			try {
				// Only do attribute scanning for public annotations; we'd run into
				// IllegalAccessExceptions otherwise, and we don't want to mess with
				// accessibility in a SecurityManager environment.
				if (Modifier.isPublic(annotationType.getModifiers())) {
					this.attributesMap.add(annotationName,
							AnnotationUtils.getAnnotationAttributes(annotation, false, true));
				}
				for (Annotation metaMetaAnnotation : annotationType.getAnnotations()) {
					recursivelyCollectMetaAnnotations(visited, metaMetaAnnotation);
				}
			}
			catch (Throwable ex) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Failed to introspect meta-annotations on [" + annotation + "]: " + ex);
				}
			}
		}
	}

}
