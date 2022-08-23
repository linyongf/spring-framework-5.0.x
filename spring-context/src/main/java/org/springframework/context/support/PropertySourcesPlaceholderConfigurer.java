/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.util.Properties;

/**
 * Specialization of {@link PlaceholderConfigurerSupport} that resolves ${...} placeholders
 * within bean definition property values and {@code @Value} annotations against the current
 * Spring {@link Environment} and its set of {@link PropertySources}.
 *
 * <p>This class is designed as a general replacement for {@code PropertyPlaceholderConfigurer}
 * introduced in Spring 3.1. It is used by default to support the {@code property-placeholder}
 * element in working against the spring-context-3.1 or higher XSD, whereas spring-context
 * versions &lt;= 3.0 default to {@code PropertyPlaceholderConfigurer} to ensure backward
 * compatibility. See the spring-context XSD documentation for complete details.
 *
 * <p>Any local properties (e.g. those added via {@link #setProperties}, {@link #setLocations}
 * et al.) are added as a {@code PropertySource}. Search precedence of local properties is
 * based on the value of the {@link #setLocalOverride localOverride} property, which is by
 * default {@code false} meaning that local properties are to be searched last, after all
 * environment property sources.
 *
 * <p>See {@link org.springframework.core.env.ConfigurableEnvironment} and related javadocs
 * for details on manipulating environment property sources.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see org.springframework.core.env.ConfigurableEnvironment
 * @see org.springframework.beans.factory.config.PlaceholderConfigurerSupport
 * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 */
public class PropertySourcesPlaceholderConfigurer extends PlaceholderConfigurerSupport implements EnvironmentAware {

	/**
	 * {@value} is the name given to the {@link PropertySource} for the set of
	 * {@linkplain #mergeProperties() merged properties} supplied to this configurer.
	 */
	public static final String LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME = "localProperties";

	/**
	 * {@value} is the name given to the {@link PropertySource} that wraps the
	 * {@linkplain #setEnvironment environment} supplied to this configurer.
	 */
	public static final String ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME = "environmentProperties";


	@Nullable
	private MutablePropertySources propertySources;

	@Nullable
	private PropertySources appliedPropertySources;

	@Nullable
	private Environment environment;


	/**
	 * Customize the set of {@link PropertySources} to be used by this configurer.
	 * <p>Setting this property indicates that environment property sources and
	 * local properties should be ignored.
	 * @see #postProcessBeanFactory
	 */
	public void setPropertySources(PropertySources propertySources) {
		this.propertySources = new MutablePropertySources(propertySources);
	}

	/**
	 * {@code PropertySources} from the given {@link Environment}
	 * will be searched when replacing ${...} placeholders.
	 * @see #setPropertySources
	 * @see #postProcessBeanFactory
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}


	/**
	 * Processing occurs by replacing ${...} placeholders in bean definitions by resolving each
	 * against this configurer's set of {@link PropertySources}, which includes:
	 * <ul>
	 * <li>all {@linkplain org.springframework.core.env.ConfigurableEnvironment#getPropertySources
	 * environment property sources}, if an {@code Environment} {@linkplain #setEnvironment is present}
	 * <li>{@linkplain #mergeProperties merged local properties}, if {@linkplain #setLocation any}
	 * {@linkplain #setLocations have} {@linkplain #setProperties been}
	 * {@linkplain #setPropertiesArray specified}
	 * <li>any property sources set by calling {@link #setPropertySources}
	 * </ul>
	 * <p>If {@link #setPropertySources} is called, <strong>environment and local properties will be
	 * ignored</strong>. This method is designed to give the user fine-grained control over property
	 * sources, and once set, the configurer makes no assumptions about adding additional sources.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.propertySources == null) {
			/**
			 * 1.初始化 MutablePropertySources
			 * 首先通过 this.environment 来初始化 MutablePropertySources。
			 * 说明：environment 是 Spring 属性加载的基础，里面包含了 Spring 已经加载的各个属性（Environment 是 Spring 所有配置文件
			 * 转换为 KV 的基础，而后续的一系列操作都是在 environment 基础上做的进一步封装）, environment 的初始化过程不是通过后处理器
			 * 机制实现的，而是通过监听器机制完成的。在 Springboot 启动过程中准备环境时发布 ApplicationEnvironmentPreparedEvent 事件，
			 * EnvironmentPostProcessorApplicationListener 接收到事件后调用所有 EnvironmentPostProcessor.postProcessEnvironment
			 * 方法进行后处理，比如 ActiveProfilesEnvironmentPostProcessor、ConfigDataEnvironmentPostProcessor 等，其中
			 * ConfigDataEnvironmentPostProcessor 用来处理 springboot 配置文件中的属性，配置文件加载顺序：optional:classpath:/;optional:classpath:/config/，
			 * optional:file:./;optional:file:./config/;optional:file:./config/xx/ , 优先级为项目根目录下大于classpath下，
			 * 且文件夹层级越深优先级越高，另外 properties > yml
			 * 而之所以用 MutablePropertySources 封装，是因为 MutablePropertySources 还能实现单独加载自定义的额外属性的功能。
 			 */
			this.propertySources = new MutablePropertySources();
			if (this.environment != null) {
				this.propertySources.addLast(
					new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, this.environment) {
						@Override
						@Nullable
						public String getProperty(String key) {
							return this.source.getProperty(key);
						}
					}
				);
			}
			try {
				PropertySource<?> localPropertySource =
						new PropertiesPropertySource(LOCAL_PROPERTIES_PROPERTY_SOURCE_NAME, mergeProperties());
				if (this.localOverride) {
					this.propertySources.addFirst(localPropertySource);
				}
				else {
					this.propertySources.addLast(localPropertySource);
				}
			}
			catch (IOException ex) {
				throw new BeanInitializationException("Could not load properties", ex);
			}
		}

		/**
		 * 2.初始化 PropertySourcesPropertyResolver
		 * 使用 PropertySourcesPropertyResolver 对 MutablePropertySources 的操作进行进一步封装，使得操作多个文件属性对外部不感知。
		 * 当然 PropertySourcesPropertyResolver 还提供了一个重要的功能就是对变量的解析，例如，它的初始化过程会包含这样的设置：
		 * propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
		 * propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
		 * propertyResolver.setValueSeparator(this.valueSeparator);
		 * 而对于的变量定义如下：
		 * 	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
		 *	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
		 *	public static final String DEFAULT_VALUE_SEPARATOR = ":";
 		 */
		processProperties(beanFactory, new PropertySourcesPropertyResolver(this.propertySources));
		this.appliedPropertySources = this.propertySources;
	}

	/**
	 * Visit each bean definition in the given bean factory and attempt to replace ${...} property
	 * placeholders with values from the given properties.
	 */
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			final ConfigurablePropertyResolver propertyResolver) throws BeansException {

		propertyResolver.setPlaceholderPrefix(this.placeholderPrefix);
		propertyResolver.setPlaceholderSuffix(this.placeholderSuffix);
		propertyResolver.setValueSeparator(this.valueSeparator);

		/**
		 * 3.StringValueResolver 初始化
		 * StringValueResolver 存在的目的主要是对解析逻辑的进一步封装，例如通过变量 ignoreUnresolvablePlaceholders 来控制是否对变量
		 * 做解析
		 */
		StringValueResolver valueResolver = strVal -> {
			String resolved = (this.ignoreUnresolvablePlaceholders ?
					// 如果变量无法解析则忽略
					propertyResolver.resolvePlaceholders(strVal) :
					// 如果变量无法解析则抛异常
					propertyResolver.resolveRequiredPlaceholders(strVal));
			if (this.trimValues) {
				resolved = resolved.trim();
			}
			return (resolved.equals(this.nullValue) ? null : resolved);
		};

		/**
		 * 4.StringValueResolver 注册
		 * 最后将 StringValueResolver 实例注册到单例 ConfigurableListableBeanFactory 中，也就是在真正解析变量时使用的
		 * StringValueResolver 实例。
 		 */
		doProcessProperties(beanFactoryToProcess, valueResolver);
	}

	/**
	 * Implemented for compatibility with
	 * {@link org.springframework.beans.factory.config.PlaceholderConfigurerSupport}.
	 * @deprecated in favor of
	 * {@link #processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver)}
	 * @throws UnsupportedOperationException in this implementation
	 */
	@Override
	@Deprecated
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) {
		throw new UnsupportedOperationException(
				"Call processProperties(ConfigurableListableBeanFactory, ConfigurablePropertyResolver) instead");
	}

	/**
	 * Return the property sources that were actually applied during
	 * {@link #postProcessBeanFactory(ConfigurableListableBeanFactory) post-processing}.
	 * @return the property sources that were applied
	 * @throws IllegalStateException if the property sources have not yet been applied
	 * @since 4.0
	 */
	public PropertySources getAppliedPropertySources() throws IllegalStateException {
		Assert.state(this.appliedPropertySources != null, "PropertySources have not yet been applied");
		return this.appliedPropertySources;
	}

}
