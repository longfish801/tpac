/*
 * TeaServerSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.parser.TeaMaker;
import spock.lang.Specification;
import spock.lang.Unroll;
import spock.lang.Shared;

/**
 * TeaServerクラスのテスト。
 * @version 1.0.00 2018/09/01
 * @author io.github.longfish801
 */
@Slf4j('LOG')
class TeaServerSpec extends Specification {
	/** TeaServer */
	@Shared TeaServer server;
	
	def setup(){
		server = new TeaServer();
	}
	
	def '宣言とTeaMakerとの関連付けを追加します。'(){
		given:
		String source;
		
		when:
		source = '''\
			#! tpac name hello
			#! xpac name hello
			'''.stripIndent();
		server.appendMakers([ new XpacMaker() ]);
		server.soak(source);
		then:
		server['tpac:name'].scalar == 'hello';
		server['xpac:name'].scalar == 'hello:x';
	}
	
	class XpacMaker implements TeaMaker {
		String decTag = 'xpac';
		def evalScalar(String raw){
			return "${raw}:x";
		}
	}
	
	def 'tpac文書を解析します。'(){
		given:
		String source;
		TeaServer.TeaServerParseException exc;
		
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
		exc = thrown(TeaServer.TeaServerParseException);
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
