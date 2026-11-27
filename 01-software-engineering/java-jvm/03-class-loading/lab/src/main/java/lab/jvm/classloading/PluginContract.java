package lab.jvm.classloading;

/**
 * Shared contract loaded by the parent (App) ClassLoader so isolated child loaders
 * can still communicate through a common interface type.
 */
public interface PluginContract {
    String execute(String input);
}
