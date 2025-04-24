package de.flowsuite.mailflowapi.common.constant;

import lombok.Getter;

@Getter
public enum Authorities {
    ADMIN("ADMIN"),
    USER("USER"),
    CLIENT("CLIENT"),
    CUSTOMERS_LIST("customers:list"),
    CUSTOMERS_READ("customers:read"),
    CUSTOMERS_WRITE("customers:write"),
    USERS_LIST("users:list"),
    USERS_READ("users:read"),
    USERS_WRITE("users:write"),
    SETTINGS_READ("settings:read"),
    SETTINGS_WRITE("settings:write"),
    RAG_URLS_LIST("rag_urls:read"),
    RAG_URLS_WRITE("rag_urls:write"),
    BLACKLIST_LIST("blacklist:list"),
    BLACKLIST_WRITE("blacklist:write"),
    MESSAGE_CATEGORIES_LIST("message_categories:list"),
    MESSAGE_CATEGORIES_WRITE("message_categories:write"),
    MESSAGE_LOG_LIST("message_log:list"),
    MESSAGE_LOG_READ("message_log:read"),
    MESSAGE_LOG_WRITE("message_log:write"),
    RESPONSE_RATINGS_LIST("response_ratings:list"),
    RESPONSE_RATINGS_READ("response_ratings:read");

    private final String authority;

    Authorities(String authority) {
        this.authority = authority;
    }
}
