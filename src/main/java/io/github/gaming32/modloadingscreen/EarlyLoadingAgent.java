package io.github.gaming32.modloadingscreen;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

public class EarlyLoadingAgent {
    public static void premain(String args, Instrumentation instrumentation) throws IOException {
        // Output localized startup log
        System.out.println(ActualLoadingScreen.translate("modloadingscreen.log.early_loading"));
        System.setProperty("mod-loading-screen.loaded", "true");

        final Path flatlafDestPath = Paths.get(".cache/mod-loading-screen/flatlaf.jar").toAbsolutePath();
        Files.createDirectories(flatlafDestPath.getParent());
        try (InputStream is = EarlyLoadingAgent.class.getClassLoader().getResourceAsStream(MlsConstants.FLATLAF_PATH)) {
            if (is == null) {
                // Log localized error if FlatLaf library cannot be found
                System.err.println(ActualLoadingScreen.translate("modloadingscreen.log.flatlaf_not_found"));
                return;
            }
            Files.copy(is, flatlafDestPath, StandardCopyOption.REPLACE_EXISTING);
        }
        // Log localized confirmation after FlatLaf jar is extracted
        System.out.println(ActualLoadingScreen.translate("modloadingscreen.log.extracted_flatlaf"));
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(flatlafDestPath.toFile()));

        // Start loading screen in early agent phase (fabricReady = false)
        ActualLoadingScreen.startLoadingScreen(false);
        // Register transformer to hook into Fabric/Quilt early entrypoints
        instrumentation.addTransformer(
            (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) ->
                MlsTransformers.instrumentClass(className, classfileBuffer),
            false
        );
    }
}