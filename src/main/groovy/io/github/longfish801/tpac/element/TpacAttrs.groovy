/*
 * TpacAttrs.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.shared.ArgmentChecker;
import io.github.longfish801.shared.ExchangeResource;

/**
 * TPAC文書の要素の属性です。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacAttrs<K, V> extends LinkedHashMap {
	/** ConfigObject */
	static final ConfigObject constants = ExchangeResource.config(TpacAttrs.class);
	
	/** {@inheritDoc} */
	V put(K key, V value){
		ArgmentChecker.checkMatchRex('属性名', key, /[^\r\n #]+/);
		ArgmentChecker.checkUniqueKey('属性名', key, this);
		ArgmentChecker.checkMatchRex('属性値', value, /[^\r\n]*/);
		return super.put(key, value);
	}
	
	/** {@inheritDoc} */
	String toString(){
		StringBuilder builder = StringBuilder.newInstance();
		this.each { String key, String value ->
			builder << constants.SIGNATTR;
			builder << key;
			if (!value.empty){
				builder << ' ';
				builder << value;
			}
			builder << "\n";
		}
		return builder.toString();
	}
}
