/*
 * TpacServerSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import spock.lang.Specification;
import spock.lang.Unroll;
import spock.lang.Shared;

/**
 * TpacServerクラスのテスト。
 * @version 1.0.00 2018/09/01
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TpacServerSpec extends Specification {
	/** TpacServer */
	@Shared TpacServer server;
	
	def setup(){
		server = new TpacServer();
	}
	
	def 'tpac文書を解析します。'(){
		given:
		String source;
		TeaServerParseException exc;
		
		when:
		source = '''\
			#! tpac
			'''.stripIndent();
		server.soak(source);
		then:
		server['tpac:'].key == 'tpac:';
		
		when:
		source = '''\
			#! tpac error
			#	_a
			'''.stripIndent();
		server.soak(source);
		then:
		exc = thrown(TeaServerParseException);
		exc.message == 'tpac文書の構築が記述誤りのため失敗しました。lineNo=2 line=#	_a';
	}
	
	@Unroll
	def 'tpac文書からパスに対応するハンドルを参照します。'(){
		given:
		String source = '''\
			#! tpac hello
			#> handle bye
			'''.stripIndent();
		server.soak(source);
		
		expect:
		server.path(path).path == expect;
		
		where:
		path						|| expect
		'/tpac:hello'				|| '/tpac:hello';
		'/tpac:hello/handle:bye'	|| '/tpac:hello/handle:bye';
	}
}
