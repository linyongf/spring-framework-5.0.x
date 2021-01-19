package my_test.bean_factory;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

@SuppressWarnings("deprecation")
public class BeanFactoryTest {

	@Test
	public void testSimpleLoad(){
		BeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("my_test/bean_factory/beanFactoryTest.xml"));
		// AbstractBeanFactory 的 getBean 方法
		MyTestBean bean = (MyTestBean) beanFactory.getBean("myTestBean");
		System.out.println(bean.getTestStr());
	}

}
