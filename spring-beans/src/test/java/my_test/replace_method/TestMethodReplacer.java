package my_test.replace_method;

import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class TestMethodReplacer implements MethodReplacer {
	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
		System.out.println("我替换了原有的方法");
		return null;
	}
}
