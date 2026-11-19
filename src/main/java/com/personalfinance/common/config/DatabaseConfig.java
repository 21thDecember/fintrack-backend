package com.personalfinance.common.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Cấu hình DataSource tương thích linh hoạt với cả URL tiêu chuẩn JDBC
 * lẫn chuỗi DATABASE_URL chuẩn từ Neon / Supabase / Railway / Heroku (postgres:// hoặc postgresql://)
 */
@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:postgres}")
    private String defaultUsername;

    @Value("${spring.datasource.password:postgres}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = rawUrl;
        String username = defaultUsername;
        String password = defaultPassword;

        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                // Đổi scheme tạm sang http để dùng java.net.URI parse an toàn
                String normalized = url.startsWith("postgres://")
                        ? "http://" + url.substring("postgres://".length())
                        : "http://" + url.substring("postgresql://".length());

                URI uri = new URI(normalized);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String query = uri.getQuery();

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }

                url = "jdbc:postgresql://" + host + ":" + port + path + (query != null ? "?" + query : "");
                log.info("Đã chuyển đổi DATABASE_URL sang JDBC URL cho PostgreSQL: jdbc:postgresql://{}:{}{}", host, port, path);
            } catch (Exception e) {
                log.warn("Không thể chuẩn hóa URI DATABASE_URL, giữ nguyên giá trị gốc: {}", e.getMessage());
            }
        }

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        config.setConnectionTimeout(20000);

        return new HikariDataSource(config);
    }
}
