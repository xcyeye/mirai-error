package xyz.xcye.mirai.event;

import net.mamoe.mirai.event.Event;

/**
 * @author xcye
 * @description
 * @date 2023-08-21 14:20:35
 */

public interface ListenBotable<T extends Event> {
    T handleEvent(T event);
}