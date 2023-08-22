package xyz.xcye.mirai.event;

import xyz.xcye.mirai.util.ReflectUtils;
import kotlin.coroutines.CoroutineContext;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xcye
 * @description
 * @date 2023-08-21 14:15:25
 */

@Slf4j
@Component
public class AuroraGlobalMiraiListenerHost extends SimpleListenerHost {

    @Autowired
    private List<ListenBotable> listenBotableList;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        super.handleException(context, exception);
    }

    /**
     * 监听所有的事件
     *
     * @param event
     */
    @EventHandler
    public Event listenAllEvent(Event event) {
        if (log.isDebugEnabled()) {
            log.debug("mirai中接收到事件，事件类型{}", event.getClass().getSimpleName());
        }
        for (ListenBotable listenable : listenBotableList) {
            // 根据泛型类型寻找合适的处理方式
            Class<?> genericType = ReflectUtils.getInterfaceGenericType(listenable, 0);
            if (event.getClass() == genericType) {
                return listenable.handleEvent(event);
            }
        }
        return defaultHandleEvent(event);
    }

    private Event defaultHandleEvent(Event event) {
        return event;
    }
}