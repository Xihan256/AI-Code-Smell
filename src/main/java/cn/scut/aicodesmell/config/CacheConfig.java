package cn.scut.aicodesmell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wanghy
 */
@Configuration
public class CacheConfig {

    private Map<String, String> mainPackageCache;

    public void setMainPackageCache(String projectId, String mainPackage) {
        if (Objects.isNull(mainPackageCache)) {
            mainPackageCache = new HashMap<>();
        }
        mainPackageCache.put(projectId, mainPackage);
    }

    public String getMainPackageCache(String projectId) {
        if (CollectionUtils.isEmpty(mainPackageCache)) {
            return null;
        }
        return mainPackageCache.get(projectId);
    }
}
