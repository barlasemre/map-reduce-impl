package hello;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordsRepository extends MongoRepository<Words, String> {

   

}
