package fr.theskyblockman.quickmorph.translation;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Language {
    public YamlConfiguration rawFileContent = new YamlConfiguration();
    public boolean hasError = false;

    public Language(String langName) {
        InputStream is = getClass().getResourceAsStream("/lang/" + langName + ".yml");
        assert is != null;
        try {
            rawFileContent.loadFromString(CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8)));
        } catch (IOException | InvalidConfigurationException ignore) {
            hasError = true;
        }
    }

    public String getString(String id) {
        if (hasError) return "Translation error !";
        return rawFileContent.getString(id);
    }
}
