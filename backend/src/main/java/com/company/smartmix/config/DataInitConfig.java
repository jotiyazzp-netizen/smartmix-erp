package com.company.smartmix.config;

import com.company.smartmix.auth.User;
import com.company.smartmix.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 数据初始化配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 创建默认管理员账户
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setEmail("admin@smartmix.com");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
                log.info("默认管理员账户创建成功: admin / admin123");
            }

            // 创建默认普通用户
            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRealName("普通用户");
                user.setEmail("user@smartmix.com");
                user.setRole("USER");
                user.setEnabled(true);
                userRepository.save(user);
                log.info("默认普通用户创建成功: user / user123");
            }
        };
    }
}
