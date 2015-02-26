package tw.jms.forever.dao;

import org.springframework.data.repository.CrudRepository;

import tw.jms.forever.persist.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	User findTopByEmail(String email);
}
