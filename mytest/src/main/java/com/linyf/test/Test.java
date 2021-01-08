package com.linyf.test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	public static void main(String[] args) {
		BeanFactory xmlBeanFactory = new ClassPathXmlApplicationContext("application.xml");
		TestBean testBean = (TestBean) xmlBeanFactory.getBean("testBean");
		testBean.method();
	}
}
