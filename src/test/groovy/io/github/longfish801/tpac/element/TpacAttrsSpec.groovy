/*
 * TpacAttrsSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import spock.lang.Specification;

/**
 * TpacAttrsクラスのテスト。
 * @version 1.0.00 2017/07/04
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacAttrsSpec extends Specification {
	def '要素を追加します'(){
		given:
		TpacAttrs attrs =  new TpacAttrs();
		
		when:
		attrs['abc'] = 'def';
		then:
		noExceptionThrown();
		
		when:
		attrs['abc'] = 'def';
		then:
		thrown(IllegalArgumentException);
		
		when:
		attrs['a#c'] = 'def';
		then:
		thrown(IllegalArgumentException);
		
		when:
		attrs['a c'] = 'def';
		then:
		thrown(IllegalArgumentException);
		
		when:
		attrs['def'] = '';
		then:
		noExceptionThrown();
	}
	
	def '文字列表現を返します'(){
		given:
		TpacAttrs attrs =  null;
		
		when:
		attrs =  new TpacAttrs();
		then:
		attrs.toString() == "";
		
		when:
		attrs =  new TpacAttrs();
		attrs['abc'] = 'def';
		then:
		attrs.toString() == "#-abc def\n";
		
		when:
		attrs =  new TpacAttrs();
		attrs['abc'] = '';
		then:
		attrs.toString() == "#-abc\n";
		
		when:
		attrs =  new TpacAttrs();
		attrs['abc'] = '';
		attrs['def'] = 'ghi';
		then:
		attrs.toString() == "#-abc\n#-def ghi\n";
	}
}
