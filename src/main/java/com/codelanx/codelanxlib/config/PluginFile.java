/*
 * Copyright (C) 2014 Codelanx, All Rights Reserved
 *
 * This work is licensed under a Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 *
 * This program is protected software: You are free to distrubute your
 * own use of this software under the terms of the Creative Commons BY-NC-ND
 * license as published by Creative Commons in the year 2014 or as published
 * by a later date. You may not provide the source files or provide a means
 * of running the software outside of those licensed to use it.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the Creative Commons BY-NC-ND license
 * long with this program. If not, see <https://creativecommons.org/licenses/>.
 */
package com.codelanx.codelanxlib.config;

import com.codelanx.codelanxlib.annotation.PluginClass;
import com.codelanx.codelanxlib.annotation.RelativePath;
import com.codelanx.codelanxlib.util.AnnotationUtil;
import com.codelanx.codelanxlib.util.DebugUtil;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class description for {@link PluginFile}
 *
 * @since 1.0.0
 * @author 1Rogue
 * @version 1.0.0
 * 
 * @param <E> Represents the type of the implementing enum
 */
public interface PluginFile<E extends Enum<E> & PluginFile<E>> {

    /**
     * Returns the save location for passed {@link PluginFile} argument
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @param clazz An implementing class with the {@link PluginClass} and
     *              {@link RelativePath} annotations
     * @return A {@link File} pointing to the location containing saved values
     *           for this configuration type
     */
    public static File getFileLocation(Class<? extends PluginFile> clazz) {
        if (!(AnnotationUtil.hasAnnotation(clazz, PluginClass.class)
                && AnnotationUtil.hasAnnotation(clazz, RelativePath.class))) {
            throw new IllegalStateException("'" + clazz.getName() + "' is missing either PluginClass or RelativePath annotations!");
        }
        return new File(AnnotationUtil.getPlugin(clazz).getDataFolder(),
                clazz.getAnnotation(RelativePath.class).value());
    }

    /**
     * The YAML path to store this value in
     *
     * @since 0.1.0
     * @version 0.1.0
     *
     * @return The path to the YAML value
     */
    public String getPath();

    /**
     * Returns the default value of the key
     *
     * @since 1.0.0
     * @version 1.0.0
     *
     * @return The key's default value
     */
    public Object getDefault();

    /**
     * Returns the relevant {@link FileConfiguration} for this config.
     * 
     * @since 0.1.0
     * @version 0.1.0
     * 
     * @return The internal {@link FileConfiguration} of this {@link Config}
     */
    public FileConfiguration getConfig();

    /**
     * Loads the lang values from the configuration file. Safe to use for
     * reloading.
     *
     * @since 0.1.0
     * @version 0.1.0
     *
     * @return The relevant {@link FileConfiguration} for all the lang info
     */
    default public FileConfiguration init() {
        if (!(AnnotationUtil.hasAnnotation(this.getClass(), PluginClass.class)
                && AnnotationUtil.hasAnnotation(this.getClass(), RelativePath.class))) {
            throw new IllegalStateException("'" + this.getClass().getName() + "' is missing either PluginClass or RelativePath annotations!");
        }
        String path = null;
        try {
            File folder = AnnotationUtil.getPlugin(this.getClass()).getDataFolder();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File ref = new File(folder, this.getClass().getAnnotation(RelativePath.class).value());
            path = ref.getPath();
            if (!ref.exists()) {
                ref.createNewFile();
            }
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(ref);
            for (PluginFile l : this.getClass().getEnumConstants()) {
                if (!yaml.isSet(l.getPath())) {
                    yaml.set(l.getPath(), l.getDefault());
                }
            }
            yaml.save(ref);
            return yaml;
        } catch (IOException ex) {
            DebugUtil.error(String.format("Error creating lang file '%s'!", path), ex);
            return null;
        }
    }

    /**
     * Saves the current configuration from memory
     *
     * @since 1.0.0
     * @version 1.0.0
     * 
     * @throws IOException Failed to save to the file
     */
    default public void save() throws IOException {
        this.getConfig().save(PluginFile.getFileLocation(this.getClass()));
    }

}
