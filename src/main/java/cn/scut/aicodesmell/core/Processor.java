package cn.scut.aicodesmell.core;

/**
 * @author wanghy
 */
public interface Processor {
    /**
     * 推理的接口
     *
     * @param docUrl  文档地址
     * @param codeUrl 源代码地址
     */
    void generateResult(String docUrl, String codeUrl);
}
