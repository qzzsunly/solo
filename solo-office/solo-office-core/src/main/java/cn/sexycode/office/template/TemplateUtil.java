package cn.sexycode.office.template;

public class TemplateUtil {
    private static TemplateFactory templateFactory;

    static {
        templateFactory = TemplateFactory.loadTemplateFactory();
    }

    public static Template getTemplate(String templateName) {
        return templateFactory.findTemplate(templateName);
    }
}
