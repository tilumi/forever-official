package tw.jms.forever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import tw.jms.forever.dao.UserRepository;
import tw.jms.forever.persist.model.User;


@SpringBootApplication
public class Forever {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Forever.class, args);
        UserRepository userRepository = context.getBean(UserRepository.class);
        User user = new User();
        user.setEmail("tilumi0@gmail.com");
        userRepository.save(user);
        user = new User();
        user.setEmail("tilumi77@gmail.com");
        userRepository.save(user);

    }
}
