/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

import ch.njol.skript.config.*;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Version;
import ch.njol.skript.variables.Variables;
import com.ultreon.libs.events.v0.EventPriority;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Important: don't save values from the config, a '/skript reload config/configs/all' won't work correctly otherwise!
 *
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public class SkriptConfig {
    @Nullable
    static Config mainConfig;
    static Collection<Config> configs = new ArrayList<Config>();

    static final Option<String> version = new Option<String>("version", Skript.getVersion().toString())
            .optional(true);

    public static final Option<String> language = new Option<String>("language", "english")
            .optional(true)
            .setter(s -> {
                if (!Language.load(s)) {
                    Skript.error("No language file found for '" + s + "'!");
                }
            });

    static final Option<Integer> updaterDownloadTries = new Option<Integer>("updater download tries", 7)
            .optional(true);

    public static final Option<Boolean> enableEffectCommands = new Option<Boolean>("enable effect commands", false);
    public static final Option<String> effectCommandToken = new Option<String>("effect command token", "!");
    public static final Option<Boolean> allowOpsToUseEffectCommands = new Option<Boolean>("allow ops to use effect commands", false);

    /*
     * @deprecated Will be removed in 2.8.0. Use {@link #logEffectCommands} instead.
     */
    @Deprecated
    public static final Option<Boolean> logPlayerCommands = new Option<Boolean>("log player commands", false).optional(true);
    public static final Option<Boolean> logEffectCommands = new Option<Boolean>("log effect commands", false);

    // everything handled by Variables
    public static final OptionSection databases = new OptionSection("databases");

    public static final Option<Boolean> usePlayerUUIDsInVariableNames = new Option<Boolean>("use player UUIDs in variable names", false); // TODO change to true later (as well as in the default config)
    public static final Option<Boolean> enablePlayerVariableFix = new Option<Boolean>("player variable fix", true);

    @SuppressWarnings("null")
    private static final DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final Option<DateFormat> dateFormat = new Option<DateFormat>("date format", shortDateFormat, s -> {
        try {
            if (s.equalsIgnoreCase("default"))
                return null;
            return new SimpleDateFormat(s);
        } catch (final IllegalArgumentException e) {
            Skript.error("'" + s + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
        }
        return null;
    });

    public static String formatDate(final long timestamp) {
        final DateFormat f = dateFormat.value();
        synchronized (f) {
            return f.format(timestamp);
        }
    }

    static final Option<Verbosity> verbosity = new Option<Verbosity>("verbosity", Verbosity.NORMAL, new EnumParser<Verbosity>(Verbosity.class, "verbosity"))
            .setter(SkriptLogger::setVerbosity);

    public static final Option<EventPriority> defaultEventPriority = new Option<EventPriority>("plugin priority", EventPriority.NORMAL, s -> {
        try {
            return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
        } catch (final IllegalArgumentException e) {
            Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
            return null;
        }
    });


    /**
     * Maximum number of digits to display after the period for floats and doubles
     */
    public static final Option<Integer> numberAccuracy = new Option<Integer>("number accuracy", 2);

    public static final Option<Integer> maxTargetBlockDistance = new Option<Integer>("maximum target block distance", 100);

    public static final Option<Boolean> caseSensitive = new Option<Boolean>("case sensitive", false);
    public static final Option<Boolean> allowFunctionsBeforeDefs = new Option<Boolean>("allow function calls before definations", false)
            .optional(true);

    public static final Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<Boolean>("disable variable will not be saved warnings", false);
    public static final Option<Boolean> disableMissingAndOrWarnings = new Option<Boolean>("disable variable missing and/or warnings", false);
    public static final Option<Boolean> disableVariableStartingWithExpressionWarnings =
            new Option<Boolean>("disable starting a variable's name with an expression warnings", false);

    @Deprecated
    public static final Option<Boolean> enableScriptCaching = new Option<Boolean>("enable script caching", false)
            .optional(true);

    public static final Option<Boolean> keepConfigsLoaded = new Option<Boolean>("keep configs loaded", false)
            .optional(true);

    public static final Option<Boolean> addonSafetyChecks = new Option<Boolean>("addon safety checks", false)
            .optional(true);

    public static final Option<Boolean> apiSoftExceptions = new Option<Boolean>("soft api exceptions", false);

    public static final Option<Boolean> enableTimings = new Option<Boolean>("enable timings", false)
            .setter(t -> {
                if (!Skript.classExists("co.aikar.timings.Timings")) { // Check for Timings
                    if (t) // Warn the server admin that timings won't work
                        Skript.warning("Timings cannot be enabled! You are running Bukkit/Spigot, but Paper is required.");
                    SkriptTimings.setEnabled(false); // Just to be sure, deactivate timings support completely
                    return;
                }
                // If we get here, we can safely enable timings
                if (t)
                    Skript.info("Timings support enabled!");
                SkriptTimings.setEnabled(t); // Config option will be used
            });

    public static final Option<Boolean> caseInsensitiveVariables = new Option<Boolean>("case-insensitive variables", true)
            .setter(t -> Variables.caseInsensitiveVariables = t);

    public static final Option<String> scriptLoaderThreadSize = new Option<String>("script loader thread size", "0")
            .setter(s -> {
                int asyncLoaderSize;

                if (s.equalsIgnoreCase("processor count")) {
                    asyncLoaderSize = Runtime.getRuntime().availableProcessors();
                } else {
                    try {
                        asyncLoaderSize = Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        Skript.error("Invalid option: " + s);
                        return;
                    }
                }

                ScriptLoader.setAsyncLoaderSize(asyncLoaderSize);
            })
            .optional(true);

    public static final Option<Boolean> allowUnsafePlatforms = new Option<Boolean>("allow unsafe platforms", false)
            .optional(true);

    public static final Option<Boolean> logToConsoleUsingLogger = new Option<Boolean>("´log to console' writes using logger", false)
            .optional(true);

    public static final Option<Boolean> keepLastUsageDates = new Option<Boolean>("keep command last usage dates", false)
            .optional(true);

    public static final Option<Boolean> loadDefaultAliases = new Option<Boolean>("load default aliases", true)
            .optional(true);

    public static final Option<Boolean> executeFunctionsWithMissingParams = new Option<Boolean>("execute functions with missing parameters", true)
            .optional(true)
            .setter(t -> Function.executeWithNulls = t);
    /**
     * Disables the specified hook depending on the option value, or gives an error if this isn't allowed at this time.
     */
    private static void userDisableHooks(Class<? extends Hook<?>> hookClass, boolean value) {
        if (Skript.isFinishedLoadingHooks()) {
            Skript.error("Hooks cannot be disabled once the server has started. " +
                    "Please restart the server to disable the hooks.");
            return;
        }
        if (value) {
            Skript.disableHookRegistration(hookClass);
        }
    }

    public final static Option<Pattern> playerNameRegexPattern = new Option<Pattern>("player name regex pattern", Pattern.compile("[a-zA-Z0-9_]{1,16}"), s -> {
        try {
            return Pattern.compile(s);
        } catch (PatternSyntaxException e) {
            Skript.error("Invalid player name regex pattern: " + e.getMessage());
            return null;
        }
    }).optional(true);

    public static final Option<Timespan> longParseTimeWarningThreshold = new Option<Timespan>("long parse time warning threshold", new Timespan(0));

    /**
     * This should only be used in special cases
     */
    @Nullable
    public static Config getConfig() {
        return mainConfig;
    }

    // also used for reloading
    static boolean load() {
        try {
            final File oldConfigFile = new File(Skript.getInstance().getDataFolder(), "config.cfg");
            final File configFile = new File(Skript.getInstance().getDataFolder(), "config.sk");
            if (oldConfigFile.exists()) {
                if (!configFile.exists()) {
                    oldConfigFile.renameTo(configFile);
                    Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
                } else {
                    Skript.error("Found both a new and an old config, ignoring the old one");
                }
            }
            if (!configFile.exists()) {
                Skript.error("Config file 'config.sk' does not exist!");
                return false;
            }
            if (!configFile.canRead()) {
                Skript.error("Config file 'config.sk' cannot be read!");
                return false;
            }

            Config mc;
            try {
                mc = new Config(configFile, false, false, ":");
            } catch (final IOException e) {
                Skript.error("Could not load the main config: " + e.getLocalizedMessage());
                return false;
            }
            mainConfig = mc;

            String configVersion = mc.get(version.key);
            if (configVersion == null || Skript.getVersion().compareTo(new Version(configVersion)) != 0) {
                try {
                    final InputStream in = Skript.getInstance().getResource("config.sk");
                    if (in == null) {
                        Skript.error("Your config is outdated, but Skript couldn't find the newest config in its jar.");
                        return false;
                    }
                    final Config newConfig = new Config(in, "Skript.jar/config.sk", false, false, ":");
                    in.close();

                    boolean forceUpdate = false;

                    if (mc.getMainNode().get("database") != null) { // old database layout
                        forceUpdate = true;
                        try {
                            final SectionNode oldDB = (SectionNode) mc.getMainNode().get("database");
                            assert oldDB != null;
                            final SectionNode newDBs = (SectionNode) newConfig.getMainNode().get(databases.key);
                            assert newDBs != null;
                            final SectionNode newDB = (SectionNode) newDBs.get("database 1");
                            assert newDB != null;

                            newDB.setValues(oldDB);

                            // '.db' was dynamically added before
                            final String file = newDB.getValue("file");
                            assert file != null;
                            if (!file.endsWith(".db"))
                                newDB.set("file", file + ".db");

                            final SectionNode def = (SectionNode) newDBs.get("default");
                            assert def != null;
                            def.set("backup interval", mc.get("variables backup interval"));
                        } catch (final Exception e) {
                            Skript.error("An error occurred while trying to update the config's database section.");
                            Skript.error("You'll have to update the config yourself:");
                            Skript.error("Open the new config.sk as well as the created backup, and move the 'database' section from the backup to the start of the 'databases' section");
                            Skript.error("of the new config (i.e. the line 'databases:' should be directly above 'database:'), and add a tab in front of every line that you just copied.");
                            return false;
                        }
                    }

                    if (newConfig.setValues(mc, version.key, databases.key) || forceUpdate) { // new config is different
                        final File bu = FileUtils.backup(configFile);
                        newConfig.getMainNode().set(version.key, Skript.getVersion().toString());
                        if (mc.getMainNode().get(databases.key) != null)
                            newConfig.getMainNode().set(databases.key, mc.getMainNode().get(databases.key));
                        mc = mainConfig = newConfig;
                        mc.save(configFile);
                        Skript.info("Your configuration has been updated to the latest version. A backup of your old config file has been created as " + bu.getName());
                    } else { // only the version changed
                        mc.getMainNode().set(version.key, Skript.getVersion().toString());
                        mc.save(configFile);
                    }
                } catch (final IOException e) {
                    Skript.error("Could not load the new config from the jar file: " + e.getLocalizedMessage());
                }
            }

            mc.load(SkriptConfig.class);

//			if (!keepConfigsLoaded.value())
//				mainConfig = null;
        } catch (final RuntimeException e) {
            Skript.exception(e, "An error occurred while loading the config");
            return false;
        }
        return true;
    }

}
