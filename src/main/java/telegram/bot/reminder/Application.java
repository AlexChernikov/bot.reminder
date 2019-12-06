package telegram.bot.reminder;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
	public static ReplicatedMap<Long, Client> chatIdAndWorkStage = hazelcastInstance.getReplicatedMap("chatIdAndWorkStage");

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
