package net.wzz.bluearchivescraft.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class BlueArchivesLogger {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Logger getLogger() {
        return LOGGER;
    }
}
