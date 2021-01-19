package my_test.custom_ele;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 *  目的是将组件注册到 Spring 容器
 */
public class MyNamespaceHandler extends NamespaceHandlerSupport {
	public void init() {
		// 当遇到自定义标签<user:aaa 这样类似于以 user 开头的元素，就会把这个元素扔给对应的 UserBeanDefinitionParser 去解析
		// 当前示例中只支持<myname:user 的写法，也可以在这里注册多个标签解析器，如<myname:A、<myname:B 等，
		// 使得 myname 的命名空间中可以支持多种标签解析
		// 注册后，命名空间处理器就可以根据标签的不同来调用不同的解析器进行解析
		registerBeanDefinitionParser("user", new UserBeanDefinitionParser());
	}
}
