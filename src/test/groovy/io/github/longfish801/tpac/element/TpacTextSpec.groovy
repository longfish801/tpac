/*
 * TpacTextSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import spock.lang.Specification;
import spock.lang.Shared;

/**
 * TpacTextクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacTextSpec extends Specification {
	@Shared TpacText text;
	
	def setup(){
		text = [] as TpacText;
	}
	
	def '各行の末尾に改行を付与して連結したテキストを返します。'(){
		expect:
		text.toString() == '';
		
		when:
		text << 'test';
		then:
		text.toString() == "test${System.lineSeparator()}";
		
		when:
		text << 'test2';
		then:
		text.toString() == "test${System.lineSeparator()}test2${System.lineSeparator()}";
	}
	
	def '文字列表現を出力します。'(){
		given:
		StringWriter writer;
		
		when:
		writer = new StringWriter();
		text.write(writer);
		then:
		writer.toString() == '';
		
		when:
		text << 'test';
		writer = new StringWriter();
		text.write(writer);
		then:
		writer.toString() == "test${System.lineSeparator()}";
		
		when:
		text << '# test2';
		writer = new StringWriter();
		text.write(writer);
		then:
		writer.toString() == "test${System.lineSeparator()}\t# test2${System.lineSeparator()}";
		
		when:
		text << '	# test3';
		writer = new StringWriter();
		text.write(writer);
		then:
		writer.toString() == "test${System.lineSeparator()}\t# test2${System.lineSeparator()}\t\t# test3${System.lineSeparator()}";
		
		when:
		text.write(null);
		then:
		thrown(IllegalArgumentException);
	}
}
