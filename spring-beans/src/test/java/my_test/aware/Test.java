package my_test.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class Test implements BeanFactoryAware {

	private BeanFactory beanFactory;

	// 声明 bean 的时候 Spring 会自动注入 BeanFactory
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void testAware(){
		// 通过 hello 这个 bean id 从 beanFactory 获取实例
		Hello hello = (Hello) beanFactory.getBean("hello");
		hello.say();
	}
}
