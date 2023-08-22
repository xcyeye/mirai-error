package xyz.xcye.mirai;

import xyz.xcye.mirai.properties.MiraiProperties;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.utils.BotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * @author xcye
 * @description
 * @date 2023-08-18 18:50:25
 */

@Slf4j
@Configuration
@EnableConfigurationProperties({MiraiProperties.class})
public class AuroraMiraiAutoConfiguration {

    @Autowired
    private MiraiProperties miraiProperties;

    private static <T> T copyProperties(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }
        T t = null;
        try {
            t = target.newInstance();
            org.springframework.beans.BeanUtils.copyProperties(source, t);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("拷贝bean出现异常{}", e.getMessage(), e);
        }
        return t;
    }

    /**
     * 配置默认的机器人配置
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public BotConfiguration defaultBotConfiguration() {
        BotConfiguration configuration = new BotConfiguration();
        return null;
    }

    @Bean
    public List<Map<Long, Bot>> botMapList() {
        if (miraiProperties.getBotInfoList() == null || miraiProperties.getBotInfoList().isEmpty()) {
            throw new IllegalArgumentException("当前模块并没有配置xyz.xcye.mirai配置项，请先进行配置");
        }

        List<Map<Long, MiraiProperties.QQBotInfo>> botInfoList = miraiProperties.getBotInfoList();
        List<Map<Long, Bot>> botMapList = new ArrayList<>();
        botInfoList.forEach(list -> list.forEach((k, v) -> {
            Map<Long, Bot> botMap = new HashMap<>();
            BotAuthorization botAuthorization = getAuthorization(v, k);
            if (botAuthorization == null) {
                throw new IllegalArgumentException("qq号" + k + "并没有指定认证方式");
            }

            BotConfiguration botConfiguration = null;
            try {
                botConfiguration = getBotConfiguration(v, k);
            } catch (IOException e) {
                log.error("qq号为" + k + "的机器人，并没有配置成功", e);
                return;
            }

            // 设置登录协议
            setProtocol(botConfiguration, v);

            // 如果是密码登录
            if (v.getAuthorizationType().name().equals(MiraiProperties.AuthorizationEnum.PASSWORD.name()) &&
                    !StringUtils.hasLength(v.getPassword())) {
                throw new IllegalArgumentException("qq号" + k + "的认证方式为密码认证，但是并没有设置密码");
            }
            Bot bot = BotFactory.INSTANCE.newBot(k, botAuthorization, botConfiguration);
            botMap.put(k, bot);
            botMapList.add(botMap);
        }));
        return botMapList;
    }

    private BotAuthorization getAuthorization(MiraiProperties.QQBotInfo info, Long qqNumber) {
        if (info.getAuthorizationType() == MiraiProperties.AuthorizationEnum.PASSWORD) {
            log.info("qq号{} 使用的认证方式为{}", qqNumber, MiraiProperties.AuthorizationEnum.PASSWORD.name());
            if (!StringUtils.hasLength(info.getPassword())) {
                log.error("qq号{} 并没有设置密码，请先设置密码", qqNumber);
                return null;
            }
            return BotAuthorization.byPassword(info.getPassword());
        }

        log.info("qq号{} 使用的认证方式为{}", qqNumber, MiraiProperties.AuthorizationEnum.QR_CODE.name());
        return BotAuthorization.byQRCode();
    }

    private BotConfiguration getBotConfiguration(MiraiProperties.QQBotInfo info, Long qqNumber) throws IOException {
        BotConfiguration configuration = copyProperties(info, BotConfiguration.class);
        // 设置设备信息
        if (!StringUtils.hasLength(info.getDeviceInfoPath())) {
            log.warn("qq号{}并为设置设备文件信息，将使用默认设备信息", qqNumber);
            info.loadDeviceInfoJson(getDeviceInfoStr(getDefaultDeviceInfoStream()));
        } else {
            log.info("将从{}文件中加载qq号为{}的设备信息", info.getDeviceInfoPath(), qqNumber);
            info.loadDeviceInfoJson(getDeviceInfoStr(getDeviceInfoStream(info.getDeviceInfoPath())));
        }

        // 设置其他的属性
        return configuration;
    }

    private void setProtocol(BotConfiguration configuration, MiraiProperties.QQBotInfo info) {
        if (Objects.equals(info.getAuthorizationType().name(), MiraiProperties.AuthorizationEnum.QR_CODE.name())) {
            switch (info.getProtocol()) {
                case IPAD:
                case ANDROID_PHONE:
                case ANDROID_PAD:
                    configuration.setProtocol(BotConfiguration.MiraiProtocol.MACOS);
                    break;
            }
        }
    }

    private String getDeviceInfoStr(InputStream inputStream) {
        InputStreamReader stream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(stream);
        StringBuilder builder = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            log.warn("从流中读取设备信息失败，将使用默认设备信息");
            return getDeviceInfoStr(getDefaultDeviceInfoStream());
        }
        return builder.toString();
    }

    private InputStream getDefaultDeviceInfoStream() {
        return AuroraMiraiAutoConfiguration.class.getResourceAsStream("/default-device.json");
    }

    private InputStream getDeviceInfoStream(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("你传入的设备信息文件{}不存在，将使用默认设备信息", filePath);
            return getDefaultDeviceInfoStream();
        }
        return Files.newInputStream(file.toPath());
    }
}