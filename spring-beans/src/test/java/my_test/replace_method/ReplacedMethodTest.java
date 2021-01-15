package my_test.replace_method;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

@SuppressWarnings("deprecation")
public class ReplacedMethodTest {
	@Test
	public void main() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("my_test/replace_method/replaceMethodTest.xml"));
		TestChangeMethod testChangeMethod = (TestChangeMethod) bf.getBean("testChangeMethod");
		testChangeMethod.changeMe();
	}
}
