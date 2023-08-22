package xyz.xcye.mirai.properties;

import lombok.Data;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @author xcye
 * @description
 * @date 2023-08-18 18:43:29
 */

@Data
@ConfigurationProperties(prefix = MiraiProperties.COTTON_MIRAI_PROPERTIES)
public class MiraiProperties {
    public final static String COTTON_MIRAI_PROPERTIES = "xyz.xcye.mirai";

    /**
     * qq机器人配置信息
     */
    private List<Map<Long, QQBotInfo>> botInfoList;

    @Data
    public static class QQBotInfo extends BotConfiguration {

        /**
         * 登录认证方式
         */
        private AuthorizationEnum authorizationType;

        /**
         * 密码，如果认证方式为密码，必须要配置密码
         */
        private String password;

        /**
         * 设备文件{@link device.json}文件的位置，不配置则使用默认设备文件
         */
        private String deviceInfoPath;
    }

    public enum AuthorizationEnum {
        PASSWORD,
        QR_CODE
    }
}