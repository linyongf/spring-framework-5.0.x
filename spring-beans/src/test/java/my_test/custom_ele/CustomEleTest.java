package my_test.custom_ele;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Spring 加载自定义的大致流程是遇到自定义标签后
 * 1. 去 Spring.handlers 和 Spring.schemas 中去找对应的 handler 和 XSD，
 * 默认位置在 /META-INF/ 下，
 * 2. 进而找到对应的handler以及解析元素的 Parser，
 * 从而完成整个自定义元素的解析
 */
public class CustomEleTest {

	@Test
	public void test() {
		BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("my_test/custom_ele/customEleTest.xml"));

		User user = (User) beanFactory.getBean("testBean");
		System.out.println(user.getUserName() + "," +user.getEmail());
	}
}
