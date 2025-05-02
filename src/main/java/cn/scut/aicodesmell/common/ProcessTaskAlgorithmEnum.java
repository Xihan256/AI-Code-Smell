package cn.scut.aicodesmell.common;

/**
 * @author wanghy
 */
public enum ProcessTaskAlgorithmEnum {
    ARDOCO("ArDoCo");
    private String algorithmName;

    ProcessTaskAlgorithmEnum(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static boolean contains(String algorithmName) {
        for (ProcessTaskAlgorithmEnum value : ProcessTaskAlgorithmEnum.values()) {
            if (value.algorithmName.equals(algorithmName)) {
                return true;
            }
        }
        return false;
    }
}
