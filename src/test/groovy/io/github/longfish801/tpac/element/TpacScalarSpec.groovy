/*
 * TpacScalarSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac.element;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.TeaServer;
import io.github.longfish801.tpac.TpacServer;
import java.util.regex.Pattern;
import spock.lang.Specification;
import spock.lang.Unroll;
import spock.lang.Shared;

/**
 * TpacScalarクラスのテスト。
 * @version 1.0.00 2018/08/26
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacScalarSpec extends Specification {
	@Shared TeaServer server;
	@Shared TeaHandle handle;
	
	def setup(){
		handle = new TpacDec().setup('tag', 'name', new TpacServer());
	}
	
	def '文字列を評価してスカラー値を返します。'(){
		given:
		def refer;
		
		when:
		refer = TpacScalar.eval('@some:thing', handle);
		then:
		refer instanceof TpacRefer;
		
		when:
		refer = TpacScalar.eval('~.+', handle);
		then:
		refer instanceof Pattern;
		refer.pattern == '.+';
		
		when:
		TpacScalar.eval(null, handle);
		then:
		thrown(IllegalArgumentException);
		
		when:
		TpacScalar.eval('@some:thing', null);
		then:
		thrown(IllegalArgumentException);
	}
	
	@Unroll
	def '文字列を評価してスカラー値を返します（Unroll）。'(){
		expect:
		TpacScalar.eval(raw, handle) == expect;
		
		where:
		raw		|| expect
		'null'	|| null;
		'true'	|| true;
		'false'	|| false;
		'15'	|| 15;
		'0'		|| 0;
		'-3'	|| -3;
		'4.5'	|| 4.5;
		'-0.22'	|| -0.22;
		'0.0'	|| 0.0;
		'^Groovy'		|| 'Groovy';
		/^Yes,\nGroovy/	|| "Yes,\nGroovy";
		'Groovy'		|| 'Groovy';
	}
	
	def 'スカラー値を tpac文書の文字列に変換します。'(){
		given:
		TpacRefer refer = TpacRefer.newInstance('some:thing', handle);
		String raw;
		IllegalArgumentException exc;
		
		when:
		raw = TpacScalar.format(refer);
		then:
		raw == '@some:thing';
		
		when:
		raw = TpacScalar.format(refer);
		then:
		raw == '@some:thing';
		
		when:
		TpacScalar.format([]);
		then:
		exc = thrown(IllegalArgumentException);
		exc.message == '文字列表現に変換できないスカラー値です。value=[] class=java.util.ArrayList';
	}
	
	@Unroll
	def 'スカラー値を tpac文書の文字列に変換します（参照以外）。'(){
		expect:
		TpacScalar.format(value) == expect;
		
		where:
		value	|| expect
		null	|| 'null';
		true	|| 'true';
		false	|| 'false';
		15		|| '15';
		0		|| '0';
		-3		|| '-3';
		4.5		|| '4.5';
		-0.22	|| '-0.22';
		0.0		|| '0.0';
		Pattern.compile('.+')	|| '~.+';
		'Groovy'	|| 	'Groovy';
	}
	
	@Unroll
	def '他のデータ型に変換される恐れのある文字列は、明示的に文字列を意味する文字列表現に変換します。'(){
		expect:
		TpacScalar.format(value) == expect;
		
		where:
		value	|| expect
		'null'	|| '^null';
		'true'	|| '^true';
		'false'	|| '^false';
		'15'	|| '^15';
		'0'		|| '^0';
		'-3'	|| '^-3';
		'4.5'	|| '^4.5';
		'-0.22'	|| '^-0.22';
		'0.0'	|| '^0.0';
		'~.+'	|| '^~.+';
		'@Groovy'	|| '^@Groovy';
		'^Groovy'	|| '^^Groovy';
		"Yes,\nGroovy"	|| 	/^Yes,\nGroovy/;
		''		|| '^';
	}
}
