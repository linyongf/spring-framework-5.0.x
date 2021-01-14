package my_test.replace_method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	public static void main(String[] args) {
		ApplicationContext bf = new ClassPathXmlApplicationContext("my_test/replace_method/replaceMethodTest.xml");
		TestChangeMethod testChangeMethod = (TestChangeMethod) bf.getBean("testChangeMethod");
		testChangeMethod.changeMe();
	}
}
