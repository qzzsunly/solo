package cn.sexycode.office.template;

import cn.sexycode.util.core.file.FileUtils;

public class FreemarkerTemplateFactory implements TemplateFactory {
    @Override
    public Template findTemplate(String templateName) {
        if (WordTemplate.WORD_SUFFIX.equalsIgnoreCase(FileUtils.toSuffix(templateName))) {
            return new FreemarkerWordTemplate(templateName);
        }
        return null;
    }
}
