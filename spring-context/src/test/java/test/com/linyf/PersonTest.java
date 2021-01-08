package test.com.linyf;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PersonTest {

	@Test
	public void testClassPath(){
		BeanFactory xmlBeanFactory = new ClassPathXmlApplicationContext("com/linyf/person.xml");
		Person person = (Person) xmlBeanFactory.getBean("person");
		person.test();
	}

}
