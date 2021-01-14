package my_test.lookup;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

@SuppressWarnings("deprecation")
public class Main {

	@Test
	public void main() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("my_test/lookup/lookupTest.xml"));
		GetBeanTest getBeanTest = (GetBeanTest) bf.getBean("getBeanTest");
		getBeanTest.showMe();
	}
}
