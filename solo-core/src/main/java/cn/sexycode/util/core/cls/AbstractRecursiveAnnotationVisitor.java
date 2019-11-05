package cn.sexycode.util.core.cls;

import java.lang.reflect.Field;
import java.security.AccessControlException;

import cn.sexycode.util.core.object.ReflectionUtils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.1.1
 */
abstract class AbstractRecursiveAnnotationVisitor extends AnnotationVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRecursiveAnnotationVisitor.class);
	protected final AnnotationAttributes attributes;

	protected final ClassLoader classLoader;


	public AbstractRecursiveAnnotationVisitor(ClassLoader classLoader, AnnotationAttributes attributes) {
		super(SoloAsmInfo.ASM_VERSION);
		this.classLoader = classLoader;
		this.attributes = attributes;
	}


	@Override
	public void visit(String attributeName, Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String attributeName, String asmTypeDescriptor) {
		String annotationType = Type.getType(asmTypeDescriptor).getClassName();
		AnnotationAttributes nestedAttributes = new AnnotationAttributes(annotationType, this.classLoader);
		this.attributes.put(attributeName, nestedAttributes);
		return new RecursiveAnnotationAttributesVisitor(annotationType, nestedAttributes, this.classLoader);
	}

	@Override
	public AnnotationVisitor visitArray(String attributeName) {
		return new RecursiveAnnotationArrayVisitor(attributeName, this.attributes, this.classLoader);
	}

	@Override
	public void visitEnum(String attributeName, String asmTypeDescriptor, String attributeValue) {
		Object newValue = getEnumValue(asmTypeDescriptor, attributeValue);
		visit(attributeName, newValue);
	}

	protected Object getEnumValue(String asmTypeDescriptor, String attributeValue) {
		Object valueToUse = attributeValue;
		try {
			Class<?> enumType = this.classLoader.loadClass(Type.getType(asmTypeDescriptor).getClassName());
			Field enumConstant = ReflectionUtils.findField(enumType, attributeValue);
			if (enumConstant != null) {
				ReflectionUtils.makeAccessible(enumConstant);
				valueToUse = enumConstant.get(null);
			}
		}
		catch (ClassNotFoundException ex) {
            LOGGER.debug("Failed to classload enum type while reading annotation metadata", ex);
		}
		catch (NoClassDefFoundError ex) {
            LOGGER.debug("Failed to classload enum type while reading annotation metadata", ex);
		}
		catch (IllegalAccessException ex) {
            LOGGER.debug("Could not access enum value while reading annotation metadata", ex);
		}
		catch (AccessControlException ex) {
            LOGGER.debug("Could not access enum value while reading annotation metadata", ex);
		}
		return valueToUse;
	}

}
