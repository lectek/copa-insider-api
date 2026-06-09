/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.context.MessageSource
 *  org.springframework.core.env.Environment
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.config.AppConfigProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value={"/config"})
public class ConfigController {
    private final AppConfigProperties appConfig;
    private final Environment environment;
    private final List<ConfigItem> baseItems = new ArrayList<ConfigItem>();
    private LocalDateTime lastRefreshTime;

    public ConfigController(AppConfigProperties appConfig, Environment environment) {
        this.appConfig = appConfig;
        this.environment = environment;
        this.buildBaseItems();
    }

    @GetMapping
    public String exibirConfiguracoes(Model model) {
        model.addAttribute("configList", this.baseItems);
        model.addAttribute("lastRefreshTime", (Object)this.lastRefreshTime);
        return "fragments/config :: config";
    }

    private void buildBaseItems() {
        this.baseItems.clear();
        this.baseItems.add(new ConfigItem("Perfil do ambiente", this.appConfig.getEnvProfileLabel()));
        this.baseItems.add(new ConfigItem("Porta da rede", this.appConfig.getNetworkPortLabel()));
        this.baseItems.add(new ConfigItem("Java vers\u00e3o", System.getProperty("java.version")));
        this.baseItems.add(new ConfigItem("Spring perfil ativo", String.join((CharSequence)", ", this.environment.getActiveProfiles())));
        this.lastRefreshTime = LocalDateTime.now();
    }

    public static class ConfigItem {
        private final String label;
        private final String value;

        public ConfigItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return this.label;
        }

        public String getValue() {
            return this.value;
        }
    }
}
