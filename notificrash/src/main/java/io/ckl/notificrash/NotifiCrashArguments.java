package io.ckl.notificrash;

import java.util.HashMap;

/**
 * Noficrash retrieve arguments interface
 */
public interface NotifiCrashArguments {

    /**
     * @return A map containing key and values for the extra arguments
     */
    HashMap<String,String> getExtraArguments();
}
