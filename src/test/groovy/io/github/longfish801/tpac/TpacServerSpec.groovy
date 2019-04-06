/*
 * TpacServerSpec.groovy
 *
 * Copyright (C) io.github.longfish801 All Rights Reserved.
 */
package io.github.longfish801.tpac;

import groovy.util.logging.Slf4j;
import io.github.longfish801.tpac.element.TeaDec;
import io.github.longfish801.tpac.element.TeaHandle;
import io.github.longfish801.tpac.parser.TeaMaker;
import io.github.longfish801.tpac.parser.TeaMakerMakeException;
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
	
	def 'tpac記法をベースとする独自記法を実装します。'(){
		given:
		String source;
		TeaDec dec;
		TeaServerParseException exc;
		TeaServer someServer;
		
		when:
		source = '''\
			#! some body 123
			#> thing
			#-important @/tpac:/handle:aaa#scalar
			#! tpac
			#> handle aaa 456
			'''.stripIndent();
		someServer = new SomeServer();
		someServer.soak(source);
		dec = someServer['some:body'];
		then:
		dec.scalar == 123;
		dec.lowers['thing:'].map.important.refer() == 456;
		
		when:
		source = '''\
			#! some thing
			'''.stripIndent();
		someServer = new SomeServer();
		someServer.soak(source);
		then:
		exc = thrown(TeaServerParseException);
		exc.cause.message == 'スカラー値が指定されていません。key=some:thing';
		
		when:
		source = '''\
			#! some how 123
			#> thing
			'''.stripIndent();
		someServer = new SomeServer();
		someServer.soak(source);
		then:
		exc = thrown(TeaServerParseException);
		exc.cause.message == 'importantが指定されていません。key=thing:';
	}
	
	class SomeServer implements TeaServer {
		TeaMaker maker(String tag){
			return (tag == 'some')? new SomeMaker() : TeaServer.super.maker(tag);
		}
	}
	
	class SomeMaker implements TeaMaker {
		TeaDec newTeaDec(String tag, String name){
			return new SomeDec();
		}
		
		TeaHandle newTeaHandle(String tag, String name, TeaHandle upper){
			return (tag == 'thing')? new ThingHandle() : TeaMaker.super.newTeaHandle(tag, name, upper);
		}
	}
	
	class SomeDec implements TeaDec {
		@Override
		void validate(){
			if (scalar == null) throw new TeaMakerMakeException("スカラー値が指定されていません。key=${key}");
		}
	}
	
	class ThingHandle implements TeaHandle {
		@Override
		void validate(){
			if (map.important == null) throw new TeaMakerMakeException("importantが指定されていません。key=${key}");
		}
	}
}
