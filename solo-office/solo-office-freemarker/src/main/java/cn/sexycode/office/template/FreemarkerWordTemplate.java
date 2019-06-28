package cn.sexycode.office.template;

import cn.sexycode.office.util.xml.XMlToDocx;
import cn.sexycode.util.core.file.FileUtils;
import cn.sexycode.util.core.file.ZipUtils;
import cn.sexycode.util.core.io.IOUtils;
import cn.sexycode.util.core.str.StringHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * @author qinzaizhen
 */
public class FreemarkerWordTemplate implements WordTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreemarkerWordTemplate.class);

    private String path;

    private String templateName;

    /**
     * 如果是文件，则传文件的完整路径
     *
     * @param path
     */
    public FreemarkerWordTemplate(String path) {
        this.path = path;
    }

    @Override
    public void toDocx(String outFile, Map<String, Object> dataModel) {

        try {
            String templateFile = path;
            String templateDir = FileUtils.withoutSuffix(templateFile);
            ZipUtils.unZip(new File(templateFile), templateDir);
            File documentFile = new File(templateDir, WordTemplate.DOC_TEXT);
            String templateData = new String(IOUtils.toByteArray(new FileInputStream(documentFile)));
            String replace = StringHelper.replace(templateData, "&lt;", "<").replace("&gt;", ">");
            new FileOutputStream(documentFile).write(replace.getBytes());
            /** 初始化配置文件 **/
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);
            /** 加载文件 **/
            configuration.setDirectoryForTemplateLoading(new File(templateDir));

            /** 加载模板 **/
            Template template = configuration.getTemplate(WordTemplate.DOC_TEXT);
            XMlToDocx.makeWord(dataModel, template, outFile, templateFile);
        } catch (Exception e) {
            LOGGER.error("转换异常", e);
        }
    }

}
