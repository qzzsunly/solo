package cn.sexycode.office.util.xml;

import cn.sexycode.office.template.WordTemplate;
import cn.sexycode.util.core.file.ZipUtils;
import freemarker.template.Template;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class XMlToDocx {

    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * 根据数据生成文本
     *
     * @param dataMap     数据
     * @param outFilePath 生成的document.xml和document.xml.rels对应的目录名称
     * @param template    模板对象
     * @throws Exception
     */
    private static void toText(Map<String, Object> dataMap, String outFilePath, Template template) throws Exception {
        File docFile = new File(outFilePath);
        FileOutputStream fos = new FileOutputStream(docFile);
        Writer out = new BufferedWriter(new OutputStreamWriter(fos), 10240);
        template.process(dataMap, out);
        if (out != null) {
            out.close();
        }
    }

    /**
     * 生成word docx
     *
     * @param dataMap        数据
     * @param ftlPath        ftl存放的目录（模板）
     * @param outDocFilePath 生成的document.xml和document.xml.rels对应的目录名称
     * @throws Exception
     */
    public static void makeWord(Map<String, Object> dataMap, Template template, String outDocFilePath,
            String sourceDocFilePath) throws Exception {
        /** 初始化配置文件 **/
        //        Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);
        //        String fileDirectory = ftlPath;
        /** 加载文件 **/
        //        configuration.setDirectoryForTemplateLoading(new File(fileDirectory));

        /** 加载模板 **/
        //        Template template = configuration.getTemplate(WordTemplate.DOC_TEXT);
        /** 指定输出word文件的路径 **/
        String outFilePath = outDocFilePath + ".xml";
        toText(dataMap, outFilePath, template);

     /*   template = configuration.getTemplate(WordTemplate.IMG_NAME);
        outFilePath = outDocFilePath + ".xml.rels";
        toText(dataMap, outFilePath, template);*/

        try {
            //该zip文件是docx重命名后的压缩文件
            ZipInputStream zipInputStream = ZipUtils
                    .wrapZipInputStream(new FileInputStream(new File(sourceDocFilePath)));
            ZipOutputStream zipOutputStream = ZipUtils
                    .wrapZipOutputStream(new FileOutputStream(new File(outDocFilePath)));
            File fileText = new File(outDocFilePath + ".xml");
            File fileImg = new File(outDocFilePath + ".xml.rels");

            Map<String, InputStream> map = new HashMap<>(1);
            map.put(WordTemplate.DOC_TEXT, new FileInputStream(fileText));
            ZipUtils.replaceItem(map, zipInputStream, zipOutputStream);
            if (fileText.exists()) {
                fileText.delete();
            }
            if (fileImg.exists()) {
                fileImg.delete();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /*  *//**
     * 生成pdf
     *//*
    public static  void makePdfByXcode(String ftlPath,String docFilePath){
        try {
            XWPFDocument document=new XWPFDocument(new FileInputStream(new File(docFilePath+".docx")));
            File outFile=new File(docFilePath+".pdf");
                if(!outFile.getParentFile().exists()){
                    outFile.getParentFile().mkdirs();
            }
            OutputStream out=new FileOutputStream(outFile);
            PdfOptions options= PdfOptions.getDefault();
            IFontProvider iFontProvider = new IFontProvider() {
                @Override
                public Font getFont(String familyName, String encoding, float size, int style, Color color) {
                    try {
                        BaseFont bfChinese = null;
                        if( OS.indexOf("linux")>=0){
                            bfChinese =  BaseFont.createFont(ftlPath+"/font/msyh.ttf", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
                        }else{
                            bfChinese =  BaseFont.createFont("C:/WINDOWS/Fonts/STSONG.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
                        }
                        Font fontChinese = new Font(bfChinese, size, style, color);
                        if (familyName != null)
                            fontChinese.setFamily(familyName);
                        return fontChinese;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
            options.fontProvider( iFontProvider );
            PdfConverter.getInstance().convert(document,out,options);

        }
        catch (  Exception e) {
            e.printStackTrace();
        }
    }

*/
}