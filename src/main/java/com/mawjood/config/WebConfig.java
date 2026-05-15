package com.mawjood.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Spring MVC configuration for internationalisation (i18n).
 *
 * Sets up three things:
 * <ol>
 *   <li><b>MessageSource</b> – loads translation strings from
 *       {@code messages.properties} (default/English) and
 *       {@code messages_ar.properties} (Arabic).</li>
 *   <li><b>LocaleResolver</b> – stores the active language in the HTTP session
 *       so switching language persists across requests. Default is English.</li>
 *   <li><b>LocaleChangeInterceptor</b> – reads the {@code ?lang=} query
 *       parameter on any request and updates the session locale accordingly.
 *       Example: {@code /?lang=ar} switches to Arabic.</li>
 * </ol>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ── MessageSource ─────────────────────────────────────────────────────────

    /** Configures where to find translation files and how to encode them. */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("UTF-8");
        // Re-check message files every second so new keys load without a server restart.
        source.setCacheSeconds(1);
        // Do not fall back to the JVM system locale if a key is missing.
        source.setFallbackToSystemLocale(false);
        // Return null (not the key name) when a message is missing — easier to spot in logs.
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    // ── Locale Resolution ─────────────────────────────────────────────────────

    /** Stores the chosen locale in the HTTP session; defaults to English. */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    /** Intercepts requests that carry a {@code lang} parameter and changes
     *  the session locale. */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /** Registers the locale-change interceptor so Spring MVC applies it. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
